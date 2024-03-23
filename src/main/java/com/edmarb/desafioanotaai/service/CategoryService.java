package com.edmarb.desafioanotaai.service;

import com.edmarb.desafioanotaai.domain.category.Category;
import com.edmarb.desafioanotaai.domain.category.CategoryDTO;
import com.edmarb.desafioanotaai.domain.category.exception.CategoryNotFoundException;
import com.edmarb.desafioanotaai.repository.CategoryRepository;
import com.edmarb.desafioanotaai.service.aws.AwsSnsService;
import com.edmarb.desafioanotaai.service.aws.MessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final AwsSnsService awsSnsService;

    public CategoryService(final CategoryRepository repository, final AwsSnsService awsSnsService){
        this.repository = repository;
        this.awsSnsService = awsSnsService;
    }

    public Category insert(CategoryDTO categoryDTO) {
        Category category = new Category(categoryDTO);

        Category newCategory = this.repository.save(category);

        this.awsSnsService.publish(new MessageDTO(newCategory.toTopicMessageJson("category")));

        return newCategory;
    }

    public List<Category> getAll() {
        return this.repository.findAll();
    }

    public Optional<Category> getById(String id) {
        return this.repository.findById(id);
    }

    public Category update(String id, CategoryDTO categoryData) {
        Category category = this.repository.findById(id).orElseThrow(CategoryNotFoundException::new);

        if(! categoryData.title().isEmpty()) category.setTitle(categoryData.title());
        if (! categoryData.description().isEmpty()) category.setDescription(categoryData.description());

        this.repository.save(category);

        this.awsSnsService.publish(new MessageDTO(category.toTopicMessageJson("category")));

        return category;
    }

    public void delete(String id) {
        Category category = this.repository.findById(id).orElseThrow(CategoryNotFoundException::new);

        this.awsSnsService.publish(new MessageDTO(category.toTopicMessageJson("delete-category")));

        this.repository.delete(category);
    }
}
