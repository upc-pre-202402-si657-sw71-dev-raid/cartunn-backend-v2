package com.thecoders.cartunnbackend.purchasing.application.internal.commandservices;

import com.thecoders.cartunnbackend.purchasing.domain.model.aggregates.Order;
import com.thecoders.cartunnbackend.purchasing.domain.model.commands.CreateOrderCommand;
import com.thecoders.cartunnbackend.purchasing.domain.model.commands.DeleteOrderCommand;
import com.thecoders.cartunnbackend.purchasing.domain.model.commands.UpdateOrderCommand;
import com.thecoders.cartunnbackend.purchasing.infrastructure.persitence.jpa.repositories.PurchasingOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCommandServiceImplTest {

    @Mock
    private PurchasingOrderRepository purchasingOrderRepository;

    @InjectMocks
    private OrderCommandServiceImpl orderCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleCreateOrderCommand_GivenValidCommand_ShouldReturnOrderId() {
        // Arrange
        CreateOrderCommand command = new CreateOrderCommand("Order 1", "Description 1", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Pending");
        Order order = new Order("Order 1", "Description 1", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Pending");
        order.setId(1L);

        when(purchasingOrderRepository.existsByName(command.name())).thenReturn(false);
        when(purchasingOrderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        Long result = orderCommandService.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result);
        verify(purchasingOrderRepository).existsByName(command.name());
        verify(purchasingOrderRepository).save(any(Order.class));
    }

    @Test
    void handleUpdateOrderCommand_GivenValidCommand_ShouldReturnUpdatedOrder() {
        // Arrange
        Long orderId = 1L;
        UpdateOrderCommand command = new UpdateOrderCommand(orderId, "Order 1 Updated", "Updated Description", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Completed");
        Order existingOrder = new Order("Order 1", "Description 1", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Pending");
        existingOrder.setId(orderId);

        when(purchasingOrderRepository.existsByNameAndIdIsNot(command.name(), command.id())).thenReturn(false);
        when(purchasingOrderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(purchasingOrderRepository.save(any(Order.class))).thenReturn(existingOrder);

        // Act
        Optional<Order> result = orderCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Order 1 Updated", result.get().getName());
        assertEquals("Updated Description", result.get().getDescription());
        assertEquals("Completed", result.get().getStatus());
        verify(purchasingOrderRepository).existsByNameAndIdIsNot(command.name(), command.id());
        verify(purchasingOrderRepository).findById(orderId);
        verify(purchasingOrderRepository).save(any(Order.class));
    }

    @Test
    void handleDeleteOrderCommand_GivenValidOrderId_ShouldDeleteOrder() {
        // Arrange
        Long orderId = 1L;
        DeleteOrderCommand command = new DeleteOrderCommand(orderId);

        when(purchasingOrderRepository.existsById(orderId)).thenReturn(true);

        // Act
        orderCommandService.handle(command);

        // Assert
        verify(purchasingOrderRepository).existsById(orderId);
        verify(purchasingOrderRepository).deleteById(orderId);
    }
}