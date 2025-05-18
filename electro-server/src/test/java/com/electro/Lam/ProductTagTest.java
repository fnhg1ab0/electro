package com.electro.Lam;

import com.electro.entity.product.Tag;
import com.electro.repository.product.TagRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductTagTest {

    @Mock
    private TagRepository tagRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register modules for JSON serialization
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }

    @Test
    public void testCreateTag() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronics");
        tag.setStatus(1); // Active status
        System.out.println("Input: " + toJson(tag));

        when(tagRepository.save(tag)).thenReturn(tag);

        // Act
        Tag savedTag = tagRepository.save(tag);
        System.out.println("Output: " + toJson(savedTag));

        // Assert
        assertNotNull(savedTag, "Saved tag should not be null");
        assertEquals("Electronics", savedTag.getName(), "Tag name should match");
        assertEquals("electronics", savedTag.getSlug(), "Tag slug should match");
        assertEquals(1, savedTag.getStatus(), "Tag status should match");
        verify(tagRepository, times(1)).save(tag);
    }

    @Test
    public void testUpdateTag() {
        // Arrange
        Tag existingTag = new Tag();
        existingTag.setId(1L);
        existingTag.setName("Electronics");
        existingTag.setSlug("electronics");
        existingTag.setStatus(1); // Active status
        System.out.println("Input (Existing Tag): " + toJson(existingTag));

        when(tagRepository.findById(1L)).thenReturn(Optional.of(existingTag));

        // Act
        Optional<Tag> tagOptional = tagRepository.findById(1L);
        assertTrue(tagOptional.isPresent(), "Tag should exist");
        Tag tagToUpdate = tagOptional.get();
        tagToUpdate.setName("Home Appliances");
        tagToUpdate.setSlug("home-appliances");
        tagToUpdate.setStatus(0); // Inactive status
        System.out.println("Input (Updated Tag): " + toJson(tagToUpdate));

        when(tagRepository.save(tagToUpdate)).thenReturn(tagToUpdate);
        Tag updatedTag = tagRepository.save(tagToUpdate);
        System.out.println("Output: " + toJson(updatedTag));

        // Assert
        assertNotNull(updatedTag, "Updated tag should not be null");
        assertEquals("Home Appliances", updatedTag.getName(), "Updated name should match");
        assertEquals("home-appliances", updatedTag.getSlug(), "Updated slug should match");
        assertEquals(0, updatedTag.getStatus(), "Updated status should match");
        verify(tagRepository, times(1)).findById(1L);
        verify(tagRepository, times(1)).save(tagToUpdate);
    }

    @Test
    public void testDeleteTag() {
        // Arrange
        Long tagId = 1L;
        System.out.println("Input (Tag ID to delete): " + tagId);
        doNothing().when(tagRepository).deleteById(tagId);

        // Act
        tagRepository.deleteById(tagId);
        System.out.println("Output: Tag with ID " + tagId + " deleted successfully.");

        // Assert
        verify(tagRepository, times(1)).deleteById(tagId);
    }

    @Test
    public void testGetAllTags() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("Electronics");
        tag.setSlug("electronics");
        tag.setStatus(1); // Active status
        List<Tag> tags = Collections.singletonList(tag);
        System.out.println("Mocked Output (All Tags): " + toJson(tags));

        when(tagRepository.findAll()).thenReturn(tags);

        // Act
        List<Tag> result = tagRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Tags list should not be null");
        assertEquals(1, result.size(), "Tags list size should be 1");
        assertEquals("Electronics", result.get(0).getName(), "Tag name should match");
        assertEquals(1, result.get(0).getStatus(), "Tag status should match");
        verify(tagRepository, times(1)).findAll();
    }

    @Test
    public void testGetTagById() {
        // Arrange
        Long tagId = 1L;
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Electronics");
        tag.setSlug("electronics");
        tag.setStatus(1); // Active status
        System.out.println("Input (Tag ID): " + tagId);
        System.out.println("Mocked Output (Tag): " + toJson(tag));

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        // Act
        Optional<Tag> tagOptional = tagRepository.findById(tagId);
        System.out.println("Output: " + toJson(tagOptional.orElse(null)));

        // Assert
        assertTrue(tagOptional.isPresent(), "Tag should exist");
        assertEquals("Electronics", tagOptional.get().getName(), "Tag name should match");
        assertEquals(1, tagOptional.get().getStatus(), "Tag status should match");
        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    public void testGetTagById_NotFound() {
        // Arrange
        Long tagId = 1L;
        System.out.println("Input (Tag ID): " + tagId);
        System.out.println("Mocked Output: Tag not found.");

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // Act
        Optional<Tag> tagOptional = tagRepository.findById(tagId);
        System.out.println("Output: " + toJson(tagOptional.orElse(null)));

        // Assert
        assertFalse(tagOptional.isPresent(), "Tag should not exist");
        verify(tagRepository, times(1)).findById(tagId);
    }
}