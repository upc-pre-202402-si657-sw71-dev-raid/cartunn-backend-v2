package com.thecoders.cartunnbackend.productRefunds.application.internal.queryservices;

import com.thecoders.cartunnbackend.productRefunds.application.internal.commandservices.ProductRefundCommandServiceImpl;
import com.thecoders.cartunnbackend.productRefunds.domain.model.aggregates.ProductRefund;
import com.thecoders.cartunnbackend.productRefunds.domain.model.queries.GetAllProductRefundsQuery;
import com.thecoders.cartunnbackend.productRefunds.domain.model.queries.GetProductRefundByIdQuery;
import com.thecoders.cartunnbackend.productRefunds.infrastructure.jpa.persistence.ProductRefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRefundQueryServiceImplTest {

    @Mock
    private ProductRefundRepository productRefundRepository;

    @InjectMocks
    private ProductRefundQueryServiceImpl productRefundQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleGetProductRefundById_GivenValidId_ShouldReturnProductRefund() {
        // Arrange
        Long productRefundId = 1L;
        GetProductRefundByIdQuery query = new GetProductRefundByIdQuery(productRefundId);
        ProductRefund productRefund = new ProductRefund("Refund 1", "Description 1", "Pending");
        productRefund.setId(productRefundId);

        when(productRefundRepository.findById(productRefundId)).thenReturn(Optional.of(productRefund));

        // Act
        Optional<ProductRefund> result = productRefundQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(productRefundId, result.get().getId());
        assertEquals("Refund 1", result.get().getTitle());
        assertEquals("Description 1", result.get().getDescription());
        assertEquals("Pending", result.get().getStatus());
        verify(productRefundRepository).findById(productRefundId);
    }

    @Test
    void handleGetAllProductRefunds_GivenValidQuery_ShouldReturnListOfProductRefunds() {
        // Arrange
        GetAllProductRefundsQuery query = new GetAllProductRefundsQuery();
        List<ProductRefund> productRefundList = List.of(
                new ProductRefund("Refund 1", "Description 1", "Pending"),
                new ProductRefund("Refund 2", "Description 2", "Approved")
        );

        when(productRefundRepository.findAll()).thenReturn(productRefundList);

        // Act
        List<ProductRefund> result = productRefundQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Refund 1", result.get(0).getTitle());
        assertEquals("Refund 2", result.get(1).getTitle());
        verify(productRefundRepository).findAll();
    }
}