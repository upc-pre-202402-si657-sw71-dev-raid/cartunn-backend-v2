package com.thecoders.cartunnbackend.purchasing.application.internal.queryservices;

import com.thecoders.cartunnbackend.purchasing.application.internal.commandservices.OrderCommandServiceImpl;
import com.thecoders.cartunnbackend.purchasing.domain.model.aggregates.Order;
import com.thecoders.cartunnbackend.purchasing.domain.model.queries.GetAllOrdersQuery;
import com.thecoders.cartunnbackend.purchasing.domain.model.queries.GetOrderByIdQuery;
import com.thecoders.cartunnbackend.purchasing.infrastructure.persitence.jpa.repositories.PurchasingOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderQueryServiceImplTest {

    @Mock
    private PurchasingOrderRepository orderRepository;

    @InjectMocks
    private OrderQueryServiceImpl orderQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleGetOrderByIdQuery_GivenValidOrderId_ShouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        GetOrderByIdQuery query = new GetOrderByIdQuery(orderId);
        Order order = new Order("Order 1", "Description 1", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Pending");
        order.setId(orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = orderQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void handleGetAllOrdersQuery_ShouldReturnListOfOrders() {
        // Arrange
        List<Order> orders = List.of(
                new Order("Order 1", "Description 1", 1234, LocalDate.now(), LocalDate.now().plusDays(5), "Pending"),
                new Order("Order 2", "Description 2", 4321, LocalDate.now().minusDays(3), LocalDate.now().plusDays(2), "Completed")
        );
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderQueryService.handle(new GetAllOrdersQuery());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }
}