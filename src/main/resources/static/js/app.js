/* ========================================
   GENERAL STORE — Core App JavaScript
   API Client, Auth, Navigation, Utilities
   ======================================== */

const API = {
    base: '',

    async request(url, options = {}) {
        const defaults = {
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include'
        };

        if (options.body instanceof FormData) {
            delete defaults.headers['Content-Type'];
        }

        const response = await fetch(this.base + url, { ...defaults, ...options });
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Request failed');
        }
        return data;
    },

    get(url) { return this.request(url); },
    post(url, body) {
        return this.request(url, {
            method: 'POST',
            body: body instanceof FormData ? body : JSON.stringify(body)
        });
    },
    put(url, body) {
        return this.request(url, {
            method: 'PUT',
            body: body instanceof FormData ? body : JSON.stringify(body)
        });
    },
    delete(url) {
        return this.request(url, { method: 'DELETE' });
    },

    upload(url, formData, method = 'POST') {
        return this.request(url, {
            method,
            body: formData,
            headers: {}
        });
    }
};

/* ========== Auth State ========== */
const Auth = {
    user: null,

    async check() {
        try {
            this.user = await API.get('/api/auth/me');
            return this.user;
        } catch {
            this.user = null;
            return null;
        }
    },

    async login(username, password) {
        const res = await API.post('/api/auth/login', { username, password });
        if (res.id) {
            this.user = res;
        }
        return res;
    },

    async register(data) {
        const res = await API.post('/api/auth/register', data);
        if (res.id) {
            this.user = res;
        }
        return res;
    },

    async logout() {
        await API.post('/api/auth/logout', {});
        this.user = null;
        window.location.href = '/login.html';
    },

    isLoggedIn() { return this.user && this.user.id; },
    isOwner() { return this.user && this.user.role === 'OWNER'; },
    isCustomer() { return this.user && this.user.role === 'CUSTOMER'; }
};

/* ========== Toast Notifications ========== */
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const icons = { success: '✓', error: '✕', info: 'ℹ' };
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${icons[type] || 'ℹ'}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOut 0.3s ease forwards';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

/* ========== Navigation ========== */
function updateNavbar() {
    const navActions = document.querySelector('.nav-actions');
    if (!navActions) return;

    if (Auth.isLoggedIn()) {
        let html = '';

        if (Auth.isCustomer()) {
            html += `
                <a href="/wishlist.html" class="nav-btn" title="Wishlist" id="nav-wishlist">
                    ♡<span class="badge hidden" id="wishlist-badge">0</span>
                </a>
                <a href="/cart.html" class="nav-btn" title="Cart" id="nav-cart">
                    🛒<span class="badge hidden" id="cart-badge">0</span>
                </a>
                <a href="/orders.html" class="nav-btn" title="My Orders">📦</a>
            `;
        }

        if (Auth.isOwner()) {
            html += `<a href="/admin.html" class="nav-btn" title="Admin Panel">⚙️</a>`;
        }

        html += `
            <button class="nav-user-btn" onclick="Auth.logout()">
                👤 ${Auth.user.username} (Sign Out)
            </button>
        `;
        navActions.innerHTML = html;

        if (Auth.isCustomer()) updateBadges();
    } else {
        navActions.innerHTML = `
            <a href="/login.html" class="nav-btn-outline" title="Sign In">Sign In</a>
            <a href="/register.html" class="nav-user-btn" title="Sign Up">Sign Up</a>
        `;
    }
}

async function updateBadges() {
    try {
        const cart = await API.get('/api/cart');
        const cartBadge = document.getElementById('cart-badge');
        if (cartBadge && cart.items) {
            const count = cart.items.length;
            cartBadge.textContent = count;
            cartBadge.classList.toggle('hidden', count === 0);
        }
    } catch {}

    try {
        const wishlist = await API.get('/api/wishlist');
        const wishBadge = document.getElementById('wishlist-badge');
        if (wishBadge && wishlist) {
            const count = wishlist.length;
            wishBadge.textContent = count;
            wishBadge.classList.toggle('hidden', count === 0);
        }
    } catch {}
}

/* ========== Scroll Navbar Effect ========== */
window.addEventListener('scroll', () => {
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        navbar.classList.toggle('scrolled', window.scrollY > 20);
    }
});

/* ========== URL Params ========== */
function getParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}

/* ========== Currency Format ========== */
function formatPrice(price) {
    return '₹' + Number(price).toLocaleString('en-IN', { minimumFractionDigits: 2 });
}

/* ========== Cart State Cache for Cards ========== */
let cartCache = {}; // productId -> quantity

async function syncCartCache() {
    if (!Auth.isLoggedIn()) {
        cartCache = {};
        renderAllCartButtons();
        return;
    }
    try {
        const cart = await API.get('/api/cart');
        cartCache = {};
        if (cart && cart.items) {
            cart.items.forEach(it => {
                cartCache[it.productId] = it.quantity;
            });
        }
        renderAllCartButtons();
    } catch {
        cartCache = {};
    }
}

function renderAllCartButtons() {
    document.querySelectorAll('[data-cart-action-for]').forEach(container => {
        const pid = Number(container.getAttribute('data-cart-action-for'));
        const inStock = container.getAttribute('data-instock') === 'true';
        container.innerHTML = getCartButtonMarkup(pid, inStock);
    });
}

function getCartButtonMarkup(productId, inStock = true) {
    if (!inStock) {
        return `<button class="btn-add-cart" disabled>Sold Out</button>`;
    }
    const qty = cartCache[productId] || 0;
    if (qty > 0) {
        return `
            <div class="card-qty-control" onclick="event.stopPropagation()">
                <button class="card-qty-btn" onclick="updateCardQty(${productId}, ${qty - 1})">−</button>
                <span class="card-qty-val">${qty}</span>
                <button class="card-qty-btn" onclick="updateCardQty(${productId}, ${qty + 1})">+</button>
            </div>
        `;
    }
    return `
        <button class="btn-add-cart" onclick="event.stopPropagation(); addToCart(${productId})">
            Add to Cart
        </button>
    `;
}

/* ========== Product Card HTML ========== */
function productCardHTML(product, options = {}) {
    const showAddToCart = options.showAddToCart !== false;
    const isClickable = options.clickable !== false;
    const showWishlist = options.showWishlist !== false;

    const imgHtml = product.imageUrl
        ? `<img src="${product.imageUrl}" alt="${product.name}" loading="lazy">`
        : `<div class="placeholder-img">🌸</div>`;

    const outOfStock = !product.inStock;
    const initialButtonHtml = showAddToCart ? getCartButtonMarkup(product.id, product.inStock) : '';

    return `
        <div class="product-card" data-id="${product.id}" style="${isClickable ? '' : 'cursor: default;'}">
            <div class="product-image">
                ${imgHtml}
                ${outOfStock ? '<div class="out-of-stock-badge">Sold Out</div>' : ''}
                ${showWishlist ? `
                    <div class="product-actions-overlay">
                        <button onclick="event.stopPropagation(); toggleWishlist(${product.id})" class="wishlist-btn" data-product="${product.id}" title="Add to Wishlist">♡</button>
                    </div>
                ` : ''}
            </div>
            <div class="product-info" ${isClickable ? `onclick="window.location.href='/product-detail.html?id=${product.id}'"` : 'style="cursor: default;"'}>
                <div class="product-category">${product.sectionName || product.categoryName || 'Luxe Care'}</div>
                <h3>${product.name}</h3>
                <div class="product-rating">
                    <span>★★★★★</span>
                    <span class="rating-count">4.9</span>
                </div>
                <p class="product-desc">${product.description || ''}</p>
                <div class="product-bottom" style="${showAddToCart ? '' : 'justify-content: flex-start;'}">
                    <span class="product-price">${formatPrice(product.price)}</span>
                    ${showAddToCart ? `
                        <div data-cart-action-for="${product.id}" data-instock="${!outOfStock}">
                            ${initialButtonHtml}
                        </div>
                    ` : ''}
                </div>
            </div>
        </div>
    `;
}

/* ========== Cart Actions ========== */
async function addToCart(productId) {
    if (!Auth.isLoggedIn()) {
        showToast('Please login to add to cart', 'error');
        window.location.href = '/login.html';
        return;
    }
    try {
        await API.post('/api/cart', { productId, quantity: 1 });
        cartCache[productId] = (cartCache[productId] || 0) + 1;
        renderAllCartButtons();
        showToast('Added to cart! 🛒', 'success');
        updateBadges();
    } catch (e) {
        showToast(e.message, 'error');
        // Refresh cache in case limit was hit and adjusted on server
        await syncCartCache();
    }
}

async function updateCardQty(productId, newQty) {
    if (!Auth.isLoggedIn()) {
        showToast('Please login to update cart', 'error');
        window.location.href = '/login.html';
        return;
    }
    try {
        if (newQty <= 0) {
            await API.delete(`/api/cart/${productId}`);
            delete cartCache[productId];
            renderAllCartButtons();
            showToast('Removed from cart', 'info');
        } else {
            await API.put('/api/cart', { productId, quantity: newQty });
            cartCache[productId] = newQty;
            renderAllCartButtons();
        }
        updateBadges();
    } catch (e) {
        showToast(e.message, 'error');
        await syncCartCache();
    }
}

/* ========== Wishlist Actions ========== */
async function toggleWishlist(productId) {
    if (!Auth.isLoggedIn()) {
        showToast('Please login to use wishlist', 'error');
        window.location.href = '/login.html';
        return;
    }
    try {
        const check = await API.get(`/api/wishlist/check/${productId}`);
        if (check.inWishlist) {
            await API.delete(`/api/wishlist/${productId}`);
            showToast('Removed from wishlist', 'info');
            document.querySelectorAll(`.wishlist-btn[data-product="${productId}"]`).forEach(btn => {
                btn.classList.remove('wishlisted');
                btn.textContent = '♡';
            });
        } else {
            await API.post('/api/wishlist', { productId });
            showToast('Added to wishlist! ♥', 'success');
            document.querySelectorAll(`.wishlist-btn[data-product="${productId}"]`).forEach(btn => {
                btn.classList.add('wishlisted');
                btn.textContent = '♥';
            });
        }
        updateBadges();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

/* ========== Search ========== */
function setupSearch() {
    const searchInput = document.getElementById('search-input');
    if (!searchInput) return;

    let timeout;
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            const q = searchInput.value.trim();
            if (q) window.location.href = `/products.html?search=${encodeURIComponent(q)}`;
        }
    });
}

/* ========== Init ========== */
async function initApp() {
    await Auth.check();
    updateNavbar();
    setupSearch();
    await syncCartCache();
}

document.addEventListener('DOMContentLoaded', initApp);
