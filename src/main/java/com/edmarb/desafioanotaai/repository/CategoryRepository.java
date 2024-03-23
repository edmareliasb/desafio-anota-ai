package com.edmarb.desafioanotaai.repository;

import com.edmarb.desafioanotaai.domain.category.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
}
