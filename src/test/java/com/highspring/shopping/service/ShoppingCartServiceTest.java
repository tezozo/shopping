package com.highspring.shopping.service;

import com.highspring.shopping.dto.ShoppingCartItemRequest;
import com.highspring.shopping.dto.UpdateShoppingCartRequest;
import com.highspring.shopping.entity.Category;
import com.highspring.shopping.entity.Item;
import com.highspring.shopping.entity.ShoppingCart;
import com.highspring.shopping.entity.ShoppingCartItem;
import com.highspring.shopping.exception.CartValidationException;
import com.highspring.shopping.repository.ItemRepository;
import com.highspring.shopping.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock ShoppingCartRepository cartRepository;
    @Mock ItemRepository itemRepository;
    @InjectMocks ShoppingCartService service;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Category category(long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Category-" + id);
        return c;
    }

    private Item item(long id, Category... categories) {
        Item item = new Item();
        item.setId(id);
        item.setName("Item-" + id);
        item.setCategories(Set.of(categories));
        return item;
    }

    private ShoppingCart activeCart(UUID id, String owner) {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(id);
        cart.setOwner(owner);
        cart.setName("Test Cart");
        return cart;
    }

    private ShoppingCart deletedCart(UUID id) {
        ShoppingCart cart = activeCart(id, "user");
        cart.setDeleted(true);
        return cart;
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Nested
    class Create {

        @Test
        void savesCartWithGivenName() {
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ShoppingCart result = service.create("alice", "My Groceries");

            assertThat(result.getOwner()).isEqualTo("alice");
            assertThat(result.getName()).isEqualTo("My Groceries");
            assertThat(result.getCreatedOn()).isNotNull();
            verify(cartRepository).save(result);
        }

        @Test
        void defaultsNameToMyCartWhenNull() {
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ShoppingCart result = service.create("alice", null);

            assertThat(result.getName()).isEqualTo("My Cart");
        }
    }

    // ── getByOwner ────────────────────────────────────────────────────────────

    @Nested
    class GetByOwner {

        @Test
        void returnsCartsFromRepository() {
            List<ShoppingCart> carts = List.of(activeCart(UUID.randomUUID(), "alice"));
            when(cartRepository.findByOwnerAndDeletedFalse("alice")).thenReturn(carts);

            assertThat(service.getByOwner("alice")).isEqualTo(carts);
        }

        @Test
        void returnsEmptyListWhenNoCartsForOwner() {
            when(cartRepository.findByOwnerAndDeletedFalse("nobody")).thenReturn(List.of());

            assertThat(service.getByOwner("nobody")).isEmpty();
        }
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Nested
    class GetById {

        @Test
        void returnsCartWhenFoundAndNotDeleted() {
            UUID id = UUID.randomUUID();
            ShoppingCart cart = activeCart(id, "alice");
            when(cartRepository.findById(id)).thenReturn(Optional.of(cart));

            assertThat(service.getById(id)).contains(cart);
        }

        @Test
        void returnsEmptyWhenCartIsDeleted() {
            UUID id = UUID.randomUUID();
            when(cartRepository.findById(id)).thenReturn(Optional.of(deletedCart(id)));

            assertThat(service.getById(id)).isEmpty();
        }

        @Test
        void returnsEmptyWhenCartNotFound() {
            UUID id = UUID.randomUUID();
            when(cartRepository.findById(id)).thenReturn(Optional.empty());

            assertThat(service.getById(id)).isEmpty();
        }
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Nested
    class Update {

        UUID cartId = UUID.randomUUID();

        @Test
        void updatesNameAndOwner() {
            ShoppingCart cart = activeCart(cartId, "alice");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest("New Name", "bob", null);
            boolean result = service.update(cartId, req);

            assertThat(result).isTrue();
            assertThat(cart.getName()).isEqualTo("New Name");
            assertThat(cart.getOwner()).isEqualTo("bob");
        }

        @Test
        void replacesItemsWhenProvided() {
            ShoppingCart cart = activeCart(cartId, "alice");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Category cat1 = category(1), cat2 = category(2), cat3 = category(3);
            Item item1 = item(1L, cat1);
            Item item2 = item(2L, cat2);
            Item item3 = item(3L, cat3);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
            when(itemRepository.findById(3L)).thenReturn(Optional.of(item3));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, List.of(
                new ShoppingCartItemRequest(1L, 2),
                new ShoppingCartItemRequest(2L, 1),
                new ShoppingCartItemRequest(3L, 3)
            ));

            boolean result = service.update(cartId, req);

            assertThat(result).isTrue();
            assertThat(cart.getItems()).hasSize(3);
        }

        @Test
        void doesNotChangeNameOrOwnerWhenNullInRequest() {
            ShoppingCart cart = activeCart(cartId, "alice");
            cart.setName("Original Name");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, null);
            service.update(cartId, req);

            assertThat(cart.getName()).isEqualTo("Original Name");
            assertThat(cart.getOwner()).isEqualTo("alice");
        }

        @Test
        void returnsFalseWhenCartNotFound() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

            boolean result = service.update(cartId, new UpdateShoppingCartRequest("x", null, null));

            assertThat(result).isFalse();
            verify(cartRepository, never()).save(any());
        }

        @Test
        void returnsFalseWhenCartIsDeleted() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(deletedCart(cartId)));

            boolean result = service.update(cartId, new UpdateShoppingCartRequest("x", null, null));

            assertThat(result).isFalse();
            verify(cartRepository, never()).save(any());
        }

        @Test
        void throwsWhenItemsSpanFewerThanThreeCategories() {
            Category cat1 = category(1), cat2 = category(2);
            Item item1 = item(1L, cat1);
            Item item2 = item(2L, cat2);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, List.of(
                new ShoppingCartItemRequest(1L, 1),
                new ShoppingCartItemRequest(2L, 1)
            ));

            assertThatThrownBy(() -> service.update(cartId, req))
                .isInstanceOf(CartValidationException.class)
                .hasMessageContaining("three different categories");
        }

        @Test
        void throwsWhenAllItemsBelongToSameCategory() {
            Category cat1 = category(1);
            Item item1 = item(1L, cat1);
            Item item2 = item(2L, cat1);

            when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
            when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, List.of(
                new ShoppingCartItemRequest(1L, 1),
                new ShoppingCartItemRequest(2L, 1)
            ));

            assertThatThrownBy(() -> service.update(cartId, req))
                .isInstanceOf(CartValidationException.class);
        }

        @Test
        void acceptsItemsWithExactlyThreeDistinctCategories() {
            ShoppingCart cart = activeCart(cartId, "alice");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Category cat1 = category(1), cat2 = category(2), cat3 = category(3);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(item(1L, cat1)));
            when(itemRepository.findById(2L)).thenReturn(Optional.of(item(2L, cat2)));
            when(itemRepository.findById(3L)).thenReturn(Optional.of(item(3L, cat3)));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, List.of(
                new ShoppingCartItemRequest(1L, 1),
                new ShoppingCartItemRequest(2L, 1),
                new ShoppingCartItemRequest(3L, 1)
            ));

            assertThatCode(() -> service.update(cartId, req)).doesNotThrowAnyException();
        }

        @Test
        void countsDistinctCategoriesAcrossMultiCategoryItems() {
            ShoppingCart cart = activeCart(cartId, "alice");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Category cat1 = category(1), cat2 = category(2), cat3 = category(3);
            // single item belonging to all three categories satisfies the rule
            Item multiCatItem = item(1L, cat1, cat2, cat3);
            when(itemRepository.findById(1L)).thenReturn(Optional.of(multiCatItem));

            UpdateShoppingCartRequest req = new UpdateShoppingCartRequest(null, null, List.of(
                new ShoppingCartItemRequest(1L, 5)
            ));

            assertThatCode(() -> service.update(cartId, req)).doesNotThrowAnyException();
        }
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Nested
    class Delete {

        UUID cartId = UUID.randomUUID();

        @Test
        void softDeletesSetsDeletedFlagAndTimestamp() {
            ShoppingCart cart = activeCart(cartId, "alice");
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = service.delete(cartId);

            assertThat(result).isTrue();
            assertThat(cart.isDeleted()).isTrue();
            assertThat(cart.getDeletedOn()).isNotNull();

            ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
            verify(cartRepository).save(captor.capture());
            assertThat(captor.getValue().isDeleted()).isTrue();
        }

        @Test
        void returnsFalseWhenCartNotFound() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

            assertThat(service.delete(cartId)).isFalse();
            verify(cartRepository, never()).save(any());
        }

        @Test
        void returnsFalseWhenCartAlreadyDeleted() {
            when(cartRepository.findById(cartId)).thenReturn(Optional.of(deletedCart(cartId)));

            assertThat(service.delete(cartId)).isFalse();
            verify(cartRepository, never()).save(any());
        }
    }

    // ── getDistinctOwners ─────────────────────────────────────────────────────

    @Nested
    class GetDistinctOwners {

        @Test
        void returnsOwnersFromRepository() {
            when(cartRepository.findDistinctOwners()).thenReturn(List.of("alice", "bob"));

            assertThat(service.getDistinctOwners()).containsExactly("alice", "bob");
        }

        @Test
        void returnsEmptyListWhenNoOwners() {
            when(cartRepository.findDistinctOwners()).thenReturn(List.of());

            assertThat(service.getDistinctOwners()).isEmpty();
        }
    }
}
