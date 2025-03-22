package com.electro;

import com.electro.dto.review.ReviewResponse;
import com.electro.service.review.ReviewServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class ReviewServiceImplTests {

    @Autowired
    private ReviewServiceImpl reviewServiceImpl;

    // ObjectMapper to convert objects to JSON and vice versa
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

    @Test
    @Transactional
    void findById_success() throws JsonProcessingException {
        // Arrange
        ReviewResponse expectedReview = createExpectedReview();

        // Act
        ReviewResponse actualReview = reviewServiceImpl.findById(1L);

        // Assert
        assertNotNull(actualReview, "Review should not be null");
        assertEquals(expectedReview.getId(), actualReview.getId(), "Review ID should be equal");
        assertEquals(expectedReview.getCreatedAt(), actualReview.getCreatedAt(), "Review created at should be equal");
        assertEquals(expectedReview.getUpdatedAt(), actualReview.getUpdatedAt(), "Review updated at should be equal");
        assertEquals(expectedReview.getUser().getId(), actualReview.getUser().getId(), "Review user ID should be equal");
        assertEquals(expectedReview.getUser().getUsername(), actualReview.getUser().getUsername(), "Review user username should be equal");
        assertEquals(expectedReview.getUser().getFullname(), actualReview.getUser().getFullname(), "Review user fullname should be equal");
        assertEquals(expectedReview.getProduct().getId(), actualReview.getProduct().getId(), "Review product ID should be equal");
        assertEquals(expectedReview.getProduct().getName(), actualReview.getProduct().getName(), "Review product name should be equal");
        assertEquals(expectedReview.getProduct().getCode(), actualReview.getProduct().getCode(), "Review product code should be equal");
        assertEquals(expectedReview.getRatingScore(), actualReview.getRatingScore(), "Review rating score should be equal");
        assertEquals(expectedReview.getContent(), actualReview.getContent(), "Review content should be equal");
        assertEquals(expectedReview.getStatus(), actualReview.getStatus(), "Review status should be equal");

        assertEquals(expectedReview, actualReview, "Review should be equal");

        // Print success message and actual data
        System.out.println("\n✅ All assertions passed successfully!");
        System.out.println("\nActual review data:");
        System.out.println(objectMapper.writeValueAsString(actualReview));
    }

    private ReviewResponse createExpectedReview() {
        ReviewResponse review = new ReviewResponse();
        review.setId(1L);
        review.setCreatedAt(Instant.parse("2021-10-03T14:16:01Z"));
        review.setUpdatedAt(Instant.parse("2021-11-17T17:55:52Z"));

        ReviewResponse.UserResponse user = new ReviewResponse.UserResponse();
        user.setId(4L);
        user.setCreatedAt(Instant.parse("2022-01-26T21:22:37Z"));
        user.setUpdatedAt(Instant.parse("2022-05-03T19:25:59Z"));
        user.setUsername("dtreat3");
        user.setFullname("Danila Treat");
        review.setUser(user);

        ReviewResponse.ProductResponse product = new ReviewResponse.ProductResponse();
        product.setId(1L);
        product.setCreatedAt(Instant.parse("2022-06-10T04:43:15Z"));
        product.setUpdatedAt(Instant.parse("2021-06-29T03:23:48Z"));
        product.setName("Dell XPS 13 9315");
        product.setCode("0003-1967");
        product.setSlug("ealdus0");
        review.setProduct(product);

        review.setRatingScore(4);
        review.setContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec posuere felis sed justo finibus, eget maximus diam rhoncus. Integer posuere tempor magna, ut dictum massa suscipit vel. Sed quis placerat neque. Etiam urna sapien, accumsan nec nulla in, condimentum venenatis ex.");
        review.setStatus(2);

        return review;
    }
}