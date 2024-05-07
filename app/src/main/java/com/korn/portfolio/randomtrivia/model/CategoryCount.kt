package com.korn.portfolio.randomtrivia.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Entity(foreignKeys = [ForeignKey(
    entity = Category::class,
    parentColumns = [Category.ID],
    childColumns = [CategoryCount.CATEGORY_ID],
    onDelete = ForeignKey.CASCADE
)])
@Serializable
data class CategoryCount(
    @PrimaryKey
    @ColumnInfo(CATEGORY_ID)
    @SerialName(CATEGORY_ID)
    val categoryId: Int,
    @Embedded
    @SerialName(CATEGORY_COUNT)
    val count: Count
) {
    companion object Column {
        const val CATEGORY_ID = "category_id"
        const val CATEGORY_COUNT = "category_question_count"
    }
}