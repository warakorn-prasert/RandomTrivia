package com.korn.portfolio.randomtrivia

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSettingChoiceGetter
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSettingChoiceGetterUnitTest {
    private val catsWithCounts: List<Pair<Category, QuestionCount>> = listOf(
        Category("Category 0", 0) to QuestionCount(0, 0, 0, 0),
        Category("Category 1", 1) to QuestionCount(1, 1, 0, 0),
        Category("Category 2", 2) to QuestionCount(1, 0, 1, 0),
        Category("Category 3", 3) to QuestionCount(1, 0, 0, 1),
        Category("Category 4", 4) to QuestionCount(1200, 300, 400, 500),
    )

    @Test
    fun `ignore categories without questions`() {
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, emptyList())
        val expectedCats = catsWithCounts.filter { it.second.total > 0 }
            .map { it.first.id }
            .toSet()
        val cats = choiceGetter.categories
            .filterNotNull()
            .map { it.id }
            .toSet()
        assertEquals(expectedCats, cats)
    }

    @Test
    fun `ignore difficulties without questions`() {
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, emptyList())
        val sampleCats = catsWithCounts
            .filter { it.second.run { total > 0 && (easy == 0 || medium == 0 || hard == 0) } }
        sampleCats.forEach { (cat, count) ->
            val expectedDiffs = mutableSetOf<Difficulty>().apply {
                if (count.easy > 0) add(Difficulty.EASY)
                if (count.medium > 0) add(Difficulty.MEDIUM)
                if (count.hard > 0) add(Difficulty.HARD)
            }
            val diffs = choiceGetter.getDifficulties(cat).filterNotNull().toSet()
            assertEquals(expectedDiffs, diffs)
        }
    }

    @Test
    fun `show correct choices when settings is empty`() {
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, emptyList())
        val sampleCatsWithCounts = catsWithCounts.filter { it.second.total > 0 } + null

        val expectedCats = sampleCatsWithCounts.map { it?.first }
        val cats = choiceGetter.categories
        assertEquals(expectedCats.toSet(), cats.toSet())

        sampleCatsWithCounts.forEach {
            val cat = it?.first
            val count =
                if (it == null) {
                    var easy = 0
                    var medium = 0
                    var hard = 0
                    sampleCatsWithCounts.filterNotNull().forEach { (_, questionCount) ->
                        easy += questionCount.easy
                        medium += questionCount.medium
                        hard += questionCount.hard
                    }
                    QuestionCount(easy + medium + hard, easy, medium, hard)
                } else {
                    it.second
                }
            val expectedDiffs: Set<Difficulty?> =
                mutableSetOf<Difficulty?>(null).apply {
                    if (count.easy > 0) add(Difficulty.EASY)
                    if (count.medium > 0) add(Difficulty.MEDIUM)
                    if (count.hard > 0) add(Difficulty.HARD)
                }
            val diffs = choiceGetter.getDifficulties(cat).toSet()
            assertEquals(expectedDiffs, diffs)

            diffs.forEach { diff ->
                val expectedMaxAmount = when (diff) {
                    Difficulty.EASY -> count.easy
                    Difficulty.MEDIUM -> count.medium
                    Difficulty.HARD -> count.hard
                    null -> count.total
                }
                val maxAmount = choiceGetter.getMaxAmount(cat, diff)
                assertEquals(expectedMaxAmount, maxAmount)
            }
        }
    }

    @Test
    fun `no choice to add setting with same category and difficulty`() {
        val sampleCatsWithCounts = catsWithCounts.filter { it.second.total > 0 } + null
        sampleCatsWithCounts.forEach {
            val cat = it?.first
            val count =
                if (it == null) {
                    var easy = 0
                    var medium = 0
                    var hard = 0
                    sampleCatsWithCounts.filterNotNull().forEach { (_, questionCount) ->
                        easy += questionCount.easy
                        medium += questionCount.medium
                        hard += questionCount.hard
                    }
                    QuestionCount(easy + medium + hard, easy, medium, hard)
                } else {
                    it.second
                }
            val diff = when {
                count.easy > 0 -> Difficulty.EASY
                count.medium > 0 -> Difficulty.MEDIUM
                else -> Difficulty.HARD
            }
            val amount = when (diff) {
                Difficulty.EASY -> count.easy
                Difficulty.MEDIUM -> count.medium
                Difficulty.HARD -> count.hard
            }
            val choiceGetter = GameSettingChoiceGetter(catsWithCounts, listOf(GameSetting(cat, diff, amount)))
            assert(diff !in choiceGetter.getDifficulties(cat))
        }
    }

    @Test
    fun `max amount of random category decreases when add new setting`() {
        val settings = mutableListOf<GameSetting>()
        var choiceGetter = GameSettingChoiceGetter(catsWithCounts, settings)

        var randomMaxAmount = choiceGetter.getMaxAmount(null, null)
        var easyMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.EASY)
        var mediumMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.MEDIUM)
        var hardMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.HARD)

        val sampleCats = catsWithCounts.filter { it.second.total > 0 }.map { it.first }
        sampleCats.forEach { cat ->
            // Instantiate new choiceGetter
            var amountForRandomDiff = 0
            val diffs = choiceGetter.getDifficulties(cat)
                .filterNotNull()
                .associateWith {
                    amountForRandomDiff ++
                    choiceGetter.getMaxAmount(cat, it) - 1
                }
            settings.addAll(diffs.map { (diff, amount) ->
                GameSetting(
                    category = cat,
                    difficulty = diff,
                    amount = amount
                )
            })
            choiceGetter = GameSettingChoiceGetter(catsWithCounts, settings)

            // Calculate new max amount and assert
            diffs.forEach { (diff, amount) ->
                when (diff) {
                    Difficulty.EASY -> {
                        val prevEasyMaxAmount = easyMaxAmount
                        easyMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.EASY)
                        assertEquals(easyMaxAmount,  prevEasyMaxAmount - amount)
                    }
                    Difficulty.MEDIUM -> {
                        val prevMediumMaxAmount = mediumMaxAmount
                        mediumMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.MEDIUM)
                        assertEquals(mediumMaxAmount, prevMediumMaxAmount - amount)
                    }
                    Difficulty.HARD -> {
                        val prevHardMaxAmount = hardMaxAmount
                        hardMaxAmount = choiceGetter.getMaxAmount(null, Difficulty.HARD)
                        assertEquals(hardMaxAmount, prevHardMaxAmount - amount)
                    }
                }
            }
            settings.add(GameSetting(cat, null, amountForRandomDiff))
            choiceGetter = GameSettingChoiceGetter(catsWithCounts, settings)

            val prevRandomMaxAmount = randomMaxAmount
            randomMaxAmount = choiceGetter.getMaxAmount(null, null)
            assertEquals(randomMaxAmount, prevRandomMaxAmount - diffs.values.sumOf { it } - amountForRandomDiff)
        }
    }

    @Test
    fun `max amount of random difficulty decreases when add new setting`() {
        val settings = mutableListOf<GameSetting>()
        var choiceGetter = GameSettingChoiceGetter(catsWithCounts, settings)

        var randomMaxAmount: Int
        var nonRandomMaxAmount: Int

        val sampleCatsWithCounts = catsWithCounts.filter { it.second.total > 0 }
        sampleCatsWithCounts.forEach { (cat, count) ->
            val diff = when {
                count.easy > 0 -> Difficulty.EASY
                count.medium > 0 -> Difficulty.MEDIUM
                else -> Difficulty.HARD
            }
            val amount = when (diff) {
                Difficulty.EASY -> count.easy
                Difficulty.MEDIUM -> count.medium
                Difficulty.HARD -> count.hard
            }

            randomMaxAmount = choiceGetter.getMaxAmount(null, null)
            nonRandomMaxAmount = choiceGetter.getMaxAmount(cat, null)

            settings.add(GameSetting(
                category = cat,
                difficulty = diff,
                amount = amount
            ))
            choiceGetter = GameSettingChoiceGetter(catsWithCounts, settings)

            val prevRandomMaxAmount = randomMaxAmount
            val prevNonRandomMaxAmount = nonRandomMaxAmount
            randomMaxAmount = choiceGetter.getMaxAmount(null, null)
            nonRandomMaxAmount = choiceGetter.getMaxAmount(cat, null)
            assertEquals(randomMaxAmount, prevRandomMaxAmount - amount)
            assertEquals(nonRandomMaxAmount, prevNonRandomMaxAmount - amount)
        }
    }

    @Test
    fun `show no choices when add all settings`() {
        val validCatsWithCounts = catsWithCounts.filter { it.second.total > 0 }

        // Get all possible settings from choice getter and assert
        var choiceGetter = GameSettingChoiceGetter(validCatsWithCounts, emptyList())
        val settings = choiceGetter.categories
            .associateWith { choiceGetter.getDifficulties(it) }
            .flatMap { (cat, diffs) ->
                diffs.map { diff ->
                    GameSetting(cat, diff, 1)
                }
            }
            .toMutableList()
        // Remove settings with random choice that makes total amount exceed QuestionCount.total
        // Above settings can have incorrect elements,
        // e.g., QuestionCount(1, 1, 0, 0) with listOf(GameSetting(cat, null, 1), GameSetting(cat, Difficulty.EASY, 1))
        validCatsWithCounts.forEach { (cat, count) ->
            val total = settings.filter { it.category == cat }.sumOf { it.amount }
            val correctTotal = count.total
            if (total > correctTotal)
                settings.removeAt(settings.indexOfFirst { it.category == cat && it.difficulty == null })
        }
        val total = settings.sumOf { it.amount }
        val correctTotal = validCatsWithCounts.sumOf { it.second.total }
        if (total > correctTotal)
            settings.removeAt(settings.indexOfFirst { it.category == null && it.difficulty == null })

        // Add valid settings assert
        choiceGetter = GameSettingChoiceGetter(validCatsWithCounts, settings)
        assert(choiceGetter.categories.isEmpty())
    }

    @Test
    fun `show no choices when no categories given`() {
        val choiceGetter = GameSettingChoiceGetter(emptyList(), emptyList())
        assert(choiceGetter.categories.isEmpty())
    }

    @Test
    fun `show no choices when random category and difficulty is in settings`() {
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, listOf(GameSetting(null, null, 1)))
        assert(choiceGetter.categories.isEmpty())
    }

    @Test
    fun `exclude category choice that has random difficulty in settings`() {
        val cat = catsWithCounts.first { it.second.total > 0 }.first
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, listOf(GameSetting(cat, null, 1)))
        assert(cat !in choiceGetter.categories)
    }

    @Test
    fun `exclude difficulty choice (and random difficulty) that has random category with that difficulty in settings`() {
        val diff = catsWithCounts.first { it.second.total > 0 }.second.let { count ->
            when {
                count.easy > 0 -> Difficulty.EASY
                count.medium > 0 -> Difficulty.MEDIUM
                else -> Difficulty.HARD
            }
        }
        val choiceGetter = GameSettingChoiceGetter(catsWithCounts, listOf(GameSetting(null, diff, 1)))
        choiceGetter.categories.forEach { cat ->
            assert(diff !in choiceGetter.getDifficulties(cat))
            assert(null !in choiceGetter.getDifficulties(cat))
        }
    }

    @Test
    fun `Random choices in settings decrease max amount when they need to`() {
        val sampleCatsWithCounts: List<Pair<Category, QuestionCount>> = listOf(
            Category("Category 0", 0) to QuestionCount(4, 1, 1, 2),
            Category("Category 1", 1) to QuestionCount(12, 5, 3, 4)
        )
        val settings = mutableListOf<GameSetting>()

        // When max amount + total amount in settings <= available amount,
        // adding a setting only decrease max amount of random category and random difficulty, and max amount per that difficulty.
        // Case 1: Add non-random setting
        settings.add(GameSetting(Category("Category 0", 0), Difficulty.EASY, 1))
        var choiceGetter = GameSettingChoiceGetter(sampleCatsWithCounts, settings)
        assertEquals(15, choiceGetter.getMaxAmount(null, null))
        assertEquals(5, choiceGetter.getMaxAmount(null, Difficulty.EASY))
        assertEquals(4, choiceGetter.getMaxAmount(null, Difficulty.MEDIUM))
        assertEquals(6, choiceGetter.getMaxAmount(null, Difficulty.HARD))
        // Case 2: Add non-random category and random difficulty setting
        settings.clear()
        settings.add(GameSetting(Category("Category 0", 0), null, 1))
        choiceGetter = GameSettingChoiceGetter(sampleCatsWithCounts, settings)
        assertEquals(15, choiceGetter.getMaxAmount(null, null))
        assertEquals(6, choiceGetter.getMaxAmount(null, Difficulty.EASY))
        assertEquals(4, choiceGetter.getMaxAmount(null, Difficulty.MEDIUM))
        assertEquals(6, choiceGetter.getMaxAmount(null, Difficulty.HARD))

        // When When max amount + total amount in settings > available amount,
        // max amount is decreased by the excess amount.
        settings.clear()
        settings.addAll(listOf(
            GameSetting(null, Difficulty.EASY, 1),
            GameSetting(null, Difficulty.MEDIUM, 2),
            GameSetting(Category("Category 0", 0), Difficulty.HARD, 2)
        ))
        choiceGetter = GameSettingChoiceGetter(sampleCatsWithCounts, settings)
        // 5 (in settings) + 12 (max amount without subtraction) = 17, but available amount = 16.
        // So max amount is decreased by 1.
        assertEquals(11, choiceGetter.getMaxAmount(Category("Category 1", 1), null))
        // Random category is also affected.
        settings.add(GameSetting(Category("Category 1", 1), null, 10))
        assertEquals(1, choiceGetter.getMaxAmount(null, Difficulty.HARD))
    }
}