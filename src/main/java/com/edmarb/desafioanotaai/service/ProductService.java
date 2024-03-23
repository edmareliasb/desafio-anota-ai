package com.edmarb.desafioanotaai.service;

import com.edmarb.desafioanotaai.domain.category.Category;
import com.edmarb.desafioanotaai.domain.category.exception.CategoryNotFoundException;
import com.edmarb.desafioanotaai.domain.product.Product;
import com.edmarb.desafioanotaai.domain.product.ProductDTO;
import com.edmarb.desafioanotaai.domain.product.ProductNotFoundException;
import com.edmarb.desafioanotaai.repository.ProductRepository;
import com.edmarb.desafioanotaai.service.aws.AwsSnsService;
import com.edmarb.desafioanotaai.service.aws.MessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final CategoryService categoryService;
    private final ProductRepository repository;

    private final AwsSnsService awsSnsService;
    public ProductService(final ProductRepository repository, final CategoryService categoryService, final AwsSnsService awsSnsService){
        this.repository = repository;
        this.categoryService = categoryService;
        this.awsSnsService = awsSnsService;
    }

    public Product insert(ProductDTO productDTO) {
        Category category = categoryService.getById(productDTO.categoryId()).orElseThrow(CategoryNotFoundException::new);
        Product product = new Product(productDTO);
        product.setCategory(category.getId());
        Product newProduct =  this.repository.save(product);

        this.awsSnsService.publish(new MessageDTO(newProduct.toTopicMessageJson("product")));

        return newProduct;
    }

    public List<Product> getAll() {
        return this.repository.findAll();
    }

    public Product update(String id, ProductDTO productData) {
        Product product = this.repository.findById(id).orElseThrow(ProductNotFoundException::new);

        if(! productData.title().isEmpty()) product.setTitle(productData.title());
        if (! productData.description().isEmpty()) product.setDescription(productData.description());
        if (productData.price() != null) product.setPrice(productData.price());

        if (productData.categoryId() != null) {
            this.categoryService.getById(productData.categoryId()).orElseThrow(CategoryNotFoundException::new);
            product.setCategory(productData.categoryId());
        }
        this.repository.save(product);

        this.awsSnsService.publish(new MessageDTO(product.toTopicMessageJson("product")));
        return product;
    }

    public void delete(String id) {
        Product product = this.repository.findById(id).orElseThrow(ProductNotFoundException::new);

        this.awsSnsService.publish(new MessageDTO(product.toTopicMessageJson("delete-product")));

        this.repository.delete(product);
    }
}
