package com.electro.Lam;

import com.electro.entity.product.Category;
import com.electro.repository.product.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductCategoryTest {

    @Mock
    private CategoryRepository categoryRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Đăng ký các module hỗ trợ, bao gồm JavaTimeModule
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "Error converting to JSON: " + e.getMessage();
        }
    }

    @Test
    public void testCreateCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        System.out.println("Input: " + toJson(category));

        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Output: " + toJson(savedCategory));

        // Assert
        assertNotNull(savedCategory, "Saved category should not be null");
        assertEquals("Electronics", savedCategory.getName(), "Category name should match");
        assertEquals("electronics", savedCategory.getSlug(), "Category slug should match");
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    public void testUpdateCategory() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Electronics");
        existingCategory.setSlug("electronics");
        System.out.println("Input (Existing Category): " + toJson(existingCategory));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));

        // Act
        Optional<Category> categoryOptional = categoryRepository.findById(1L);
        assertTrue(categoryOptional.isPresent(), "Category should exist");
        Category categoryToUpdate = categoryOptional.get();
        categoryToUpdate.setName("Updated Electronics");
        categoryToUpdate.setSlug("updated-electronics");
        System.out.println("Input (Updated Category): " + toJson(categoryToUpdate));

        when(categoryRepository.save(categoryToUpdate)).thenReturn(categoryToUpdate);
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        System.out.println("Output: " + toJson(updatedCategory));

        // Assert
        assertNotNull(updatedCategory, "Updated category should not be null");
        assertEquals("Updated Electronics", updatedCategory.getName(), "Updated name should match");
        assertEquals("updated-electronics", updatedCategory.getSlug(), "Updated slug should match");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(categoryToUpdate);
    }

    @Test
    public void testDeleteCategory() {
        // Arrange
        Long categoryId = 1L;
        System.out.println("Input (Category ID to delete): " + categoryId);
        doNothing().when(categoryRepository).deleteById(categoryId);

        // Act
        categoryRepository.deleteById(categoryId);
        System.out.println("Output: Category with ID " + categoryId + " deleted successfully.");

        // Assert
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    public void testGetAllCategories() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        List<Category> categories = Collections.singletonList(category);
        System.out.println("Mocked Output (All Categories): " + toJson(categories));

        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryRepository.findAll();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Categories list should not be null");
        assertEquals(1, result.size(), "Categories list size should be 1");
        assertEquals("Electronics", result.get(0).getName(), "Category name should match");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    public void testGetCategoryBySlug() {
        // Arrange
        String slug = "electronics";
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug(slug);
        System.out.println("Input (Slug): " + slug);
        System.out.println("Mocked Output (Category): " + toJson(category));

        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> categoryOptional = categoryRepository.findBySlug(slug);
        System.out.println("Output: " + toJson(categoryOptional.orElse(null)));

        // Assert
        assertTrue(categoryOptional.isPresent(), "Category should exist");
        assertEquals("Electronics", categoryOptional.get().getName(), "Category name should match");
        assertEquals(slug, categoryOptional.get().getSlug(), "Category slug should match");
        verify(categoryRepository, times(1)).findBySlug(slug);
    }

    @Test
    public void testGetCategoryBySlug_NotFound() {
        // Arrange
        String slug = "non-existent";
        System.out.println("Input (Slug): " + slug);
        System.out.println("Mocked Output: Category not found.");

        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // Act
        Optional<Category> categoryOptional = categoryRepository.findBySlug(slug);
        System.out.println("Output: " + toJson(categoryOptional.orElse(null)));

        // Assert
        assertFalse(categoryOptional.isPresent(), "Category should not exist");
        verify(categoryRepository, times(1)).findBySlug(slug);
    }
    
    // Additional white-box tests
    
    @Test
    public void testGetCategoryById() {
        // Arrange
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Electronics");
        category.setSlug("electronics");
        System.out.println("Input (ID): " + id);
        System.out.println("Mocked Output (Category): " + toJson(category));

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // Act
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        System.out.println("Output: " + toJson(categoryOptional.orElse(null)));

        // Assert
        assertTrue(categoryOptional.isPresent(), "Category should exist");
        assertEquals(id, categoryOptional.get().getId(), "Category ID should match");
        assertEquals("Electronics", categoryOptional.get().getName(), "Category name should match");
        verify(categoryRepository, times(1)).findById(id);
    }
    
    @Test
    public void testGetCategoryById_NotFound() {
        // Arrange
        Long id = 999L;
        System.out.println("Input (ID): " + id);
        System.out.println("Mocked Output: Category not found.");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<Category> categoryOptional = categoryRepository.findById(id);
        System.out.println("Output: " + toJson(categoryOptional.orElse(null)));

        // Assert
        assertFalse(categoryOptional.isPresent(), "Category should not exist");
        verify(categoryRepository, times(1)).findById(id);
    }
    
    @Test
    public void testCreateCategoryWithNestedAttributes() {
        // Arrange
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setDescription("Electronic devices and accessories");
        category.setStatus(1);
        System.out.println("Input: " + toJson(category));

        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Output: " + toJson(savedCategory));

        // Assert
        assertNotNull(savedCategory, "Saved category should not be null");
        assertEquals("Electronics", savedCategory.getName(), "Category name should match");
        assertEquals("electronics", savedCategory.getSlug(), "Category slug should match");
        assertEquals("Electronic devices and accessories", savedCategory.getDescription(), "Category description should match");
        assertEquals(1, savedCategory.getStatus(), "Category status should match");
        verify(categoryRepository, times(1)).save(category);
    }
    
    @Test
    public void testFindParentCategories() {
        // Arrange
        Category parentCategory1 = new Category();
        parentCategory1.setId(1L);
        parentCategory1.setName("Electronics");
        parentCategory1.setSlug("electronics");
        
        Category parentCategory2 = new Category();
        parentCategory2.setId(2L);
        parentCategory2.setName("Clothing");
        parentCategory2.setSlug("clothing");
        
        List<Category> parentCategories = Arrays.asList(parentCategory1, parentCategory2);
        System.out.println("Mocked Output (Parent Categories): " + toJson(parentCategories));

        when(categoryRepository.findByParentCategoryIsNull()).thenReturn(parentCategories);

        // Act
        List<Category> result = categoryRepository.findByParentCategoryIsNull();
        System.out.println("Output: " + toJson(result));

        // Assert
        assertNotNull(result, "Parent categories list should not be null");
        assertEquals(2, result.size(), "Parent categories list size should be 2");
        assertEquals("Electronics", result.get(0).getName(), "First category name should match");
        assertEquals("Clothing", result.get(1).getName(), "Second category name should match");
        verify(categoryRepository, times(1)).findByParentCategoryIsNull();
    }
    
    @Test
    public void testCreateNestedCategory() {
        // Arrange
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");
        parentCategory.setSlug("electronics");
        
        Category childCategory = new Category();
        childCategory.setName("Smartphones");
        childCategory.setSlug("smartphones");
        childCategory.setParentCategory(parentCategory);
        System.out.println("Input (Child Category): " + toJson(childCategory));

        when(categoryRepository.save(childCategory)).thenReturn(childCategory);

        // Act
        Category savedCategory = categoryRepository.save(childCategory);
        System.out.println("Output: " + toJson(savedCategory));

        // Assert
        assertNotNull(savedCategory, "Saved category should not be null");
        assertEquals("Smartphones", savedCategory.getName(), "Category name should match");
        assertNotNull(savedCategory.getParentCategory(), "Parent category should not be null");
        assertEquals("Electronics", savedCategory.getParentCategory().getName(), "Parent category name should match");
        verify(categoryRepository, times(1)).save(childCategory);
    }
    
    @Test
    public void testUpdateCategoryMaintainingHierarchy() {
        // Arrange
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");
        parentCategory.setSlug("electronics");
        
        Category childCategory = new Category();
        childCategory.setId(2L);
        childCategory.setName("Smartphones");
        childCategory.setSlug("smartphones");
        childCategory.setParentCategory(parentCategory);
        System.out.println("Input (Existing Child Category): " + toJson(childCategory));

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));

        // Act
        Optional<Category> categoryOptional = categoryRepository.findById(2L);
        assertTrue(categoryOptional.isPresent(), "Category should exist");
        Category categoryToUpdate = categoryOptional.get();
        categoryToUpdate.setName("Mobile Phones");
        // Keep the same parent category
        System.out.println("Input (Updated Child Category): " + toJson(categoryToUpdate));

        when(categoryRepository.save(categoryToUpdate)).thenReturn(categoryToUpdate);
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        System.out.println("Output: " + toJson(updatedCategory));

        // Assert
        assertNotNull(updatedCategory, "Updated category should not be null");
        assertEquals("Mobile Phones", updatedCategory.getName(), "Updated name should match");
        assertNotNull(updatedCategory.getParentCategory(), "Parent category should still exist");
        assertEquals("Electronics", updatedCategory.getParentCategory().getName(), "Parent category name should be unchanged");
        verify(categoryRepository, times(1)).findById(2L);
        verify(categoryRepository, times(1)).save(categoryToUpdate);
    }
    
    @Test
    public void testDeleteAllCategories() {
        // Arrange
        System.out.println("Input: Request to delete all categories");
        doNothing().when(categoryRepository).deleteAll();

        // Act
        categoryRepository.deleteAll();
        System.out.println("Output: All categories deleted successfully.");

        // Assert
        verify(categoryRepository, times(1)).deleteAll();
    }
    
//    @Test
//    public void testExistsBySlug() {
//        // Arrange
//        String slug = "electronics";
//        System.out.println("Input (Slug to check): " + slug);
//
//        when(categoryRepository.existsBySlug(slug)).thenReturn(true);
//
//        // Act
//        boolean exists = categoryRepository.existsBySlug(slug);
//        System.out.println("Output: Category with slug '" + slug + "' exists: " + exists);
//
//        // Assert
//        assertTrue(exists, "Category should exist with the given slug");
//        verify(categoryRepository, times(1)).existsBySlug(slug);
//    }
//
//    @Test
//    public void testExistsBySlug_NotExists() {
//        // Arrange
//        String slug = "non-existent";
//        System.out.println("Input (Slug to check): " + slug);
//
//        when(categoryRepository.existsBySlug(slug)).thenReturn(false);
//
//        // Act
//        boolean exists = categoryRepository.existsBySlug(slug);
//        System.out.println("Output: Category with slug '" + slug + "' exists: " + exists);
//
//        // Assert
//        assertFalse(exists, "Category should not exist with the given slug");
//        verify(categoryRepository, times(1)).existsBySlug(slug);
//    }
}