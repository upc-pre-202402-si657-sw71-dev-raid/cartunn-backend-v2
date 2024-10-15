package com.thecoders.cartunnbackend.productRefunds.application.internal.commandservices;

import com.thecoders.cartunnbackend.productRefunds.domain.model.aggregates.ProductRefund;
import com.thecoders.cartunnbackend.productRefunds.domain.model.commands.CreateProductRefundCommand;
import com.thecoders.cartunnbackend.productRefunds.domain.model.commands.UpdateProductRefundCommand;
import com.thecoders.cartunnbackend.productRefunds.infrastructure.jpa.persistence.ProductRefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRefundCommandServiceImplTest {

    @Mock
    private ProductRefundRepository productRefundRepository;

    @InjectMocks
    private ProductRefundCommandServiceImpl productRefundCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleCreateProductRefund_GivenValidCommand_ShouldSaveProductAndReturnId() {
        // Arrange
        CreateProductRefundCommand command = new CreateProductRefundCommand("Refund 1", "Description 1", "Pending");
        ProductRefund productRefund = new ProductRefund(command);
        productRefund.setId(1L);

        when(productRefundRepository.existsByTitle(command.title())).thenReturn(false);
        when(productRefundRepository.save(any(ProductRefund.class))).thenReturn(productRefund);

        // Act
        Long result = productRefundCommandService.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(productRefundRepository).existsByTitle(command.title());
        verify(productRefundRepository).save(any(ProductRefund.class));

    }

    @Test
    void handleUpdateProductRefund_GivenValidCommand_ShouldUpdateProduct() {
        // Arrange
        UpdateProductRefundCommand command = new UpdateProductRefundCommand(1L, "Updated Refund", "Updated Description", "Approved");
        ProductRefund existingProductRefund = new ProductRefund("Refund 1", "Description 1", "Pending");
        existingProductRefund.setId(1L);

        when(productRefundRepository.existsByTitleAndIdIsNot(command.title(), command.id())).thenReturn(false);
        when(productRefundRepository.findById(command.id())).thenReturn(Optional.of(existingProductRefund));
        when(productRefundRepository.save(any(ProductRefund.class))).thenReturn(existingProductRefund);

        // Act
        Optional<ProductRefund> result = productRefundCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Refund", result.get().getTitle());
        assertEquals("Updated Description", result.get().getDescription());
        assertEquals("Approved", result.get().getStatus());
        verify(productRefundRepository).existsByTitleAndIdIsNot(command.title(), command.id());
        verify(productRefundRepository).findById(command.id());
        verify(productRefundRepository).save(existingProductRefund);
    }
}