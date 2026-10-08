package com.darius.wms.product;

import com.darius.wms.common.BusinessRuleException;
import com.darius.wms.common.NotFoundException;
import com.darius.wms.common.PageResponse;
import com.darius.wms.stock.StockItemRepository;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;

    public ProductService(ProductRepository productRepository, StockItemRepository stockItemRepository) {
        this.productRepository = productRepository;
        this.stockItemRepository = stockItemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(String search, Pageable pageable) {
        Page<Product> page = StringUtils.hasText(search)
                ? productRepository.search(search.trim(), pageable)
                : productRepository.findAll(pageable);
        // One grouped query for the whole page instead of one query per product (avoids N+1)
        Map<Long, Long> totals = stockItemRepository.totalsByProduct(page.map(Product::getId).getContent());
        return PageResponse.of(page, p -> ProductResponse.from(p, totals.getOrDefault(p.getId(), 0L)));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = getEntity(id);
        return ProductResponse.from(product, stockItemRepository.sumQuantityByProduct(id));
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku().trim())) {
            throw new BusinessRuleException("A product with SKU " + request.sku() + " already exists");
        }
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product), 0);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntity(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku().trim(), id)) {
            throw new BusinessRuleException("A product with SKU " + request.sku() + " already exists");
        }
        apply(product, request);
        return ProductResponse.from(product, stockItemRepository.sumQuantityByProduct(id));
    }

    public void delete(Long id) {
        Product product = getEntity(id);
        productRepository.delete(product);
        // Flush now so a foreign-key violation (product used by stock/orders) surfaces here as a 409
        productRepository.flush();
    }

    /** Used by other services that need the managed entity. */
    @Transactional(readOnly = true)
    public Product getEntity(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    private static void apply(Product product, ProductRequest request) {
        product.setSku(request.sku().trim().toUpperCase());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setUnit(request.unit());
        product.setWeightKg(request.weightKg());
        product.setMinStockLevel(request.minStockLevel());
    }
}
