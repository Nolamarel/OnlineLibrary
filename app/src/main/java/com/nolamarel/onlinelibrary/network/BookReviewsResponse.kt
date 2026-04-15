import com.nolamarel.onlinelibrary.network.ReviewResponse

data class BookReviewsResponse(
    val averageRating: Double,
    val weightedRating: Double,
    val reviewsCount: Int,
    val reviews: List<ReviewResponse>
)