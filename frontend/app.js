window.API_BASE = "http://localhost:8081/api";
const API = window.API_BASE;
const rupee = (n) => "₹" + Number(n).toLocaleString("en-IN");

let products = [];
let cart = JSON.parse(localStorage.getItem("cart") || "{}"); // { productId: quantity }

const $ = (id) => document.getElementById(id);

// Escape text before putting it into HTML (stops XSS)
function esc(text) {
  const d = document.createElement("div");
  d.textContent = text ?? "";
  return d.innerHTML;
}

function toast(message, bad = false) {
  const t = $("toast");
  t.textContent = message;
  t.className = "toast" + (bad ? " bad" : "");
  t.hidden = false;
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => (t.hidden = true), 3500);
}

async function api(path, options) {
  const res = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || "Request failed (" + res.status + ")");
  return data;
}

/* ---------- products ---------- */
async function loadProducts() {
  try {
    products = await api("/products");
    $("shopStatus").textContent = products.length ? "" : "No products yet.";
    renderProducts();
  } catch (e) {
    $("shopStatus").textContent = "Could not reach the server. Is the backend running on " + API + " ?";
  }
}

function renderProducts() {
  $("products").innerHTML = products.map((p) => `
    <article class="card">
      <div class="art" aria-hidden="true">${esc(p.emoji || "📦")}</div>
      <div class="body">
        <h3>${esc(p.name)}</h3>
        <p>${esc(p.description)}</p>
        <div class="row">
          <span class="price">${rupee(p.price)}</span>
          <span class="stock ${p.stock < 5 ? "low" : ""}">${p.stock > 0 ? p.stock + " in stock" : "Out of stock"}</span>
        </div>
        <button class="primary" data-add="${p.id}" ${p.stock === 0 ? "disabled" : ""}>Add to cart</button>
      </div>
    </article>`).join("");
}

/* ---------- cart ---------- */
function saveCart() {
  localStorage.setItem("cart", JSON.stringify(cart));
  renderCart();
}

function addToCart(id) {
  const p = products.find((x) => x.id === id);
  const next = (cart[id] || 0) + 1;
  if (next > p.stock) return toast("Only " + p.stock + " available for " + p.name, true);
  cart[id] = next;
  saveCart();
  toast(p.name + " added to cart");
}

function changeQty(id, delta) {
  const p = products.find((x) => x.id === id);
  const next = (cart[id] || 0) + delta;
  if (next <= 0) delete cart[id];
  else if (next > p.stock) return toast("Only " + p.stock + " available", true);
  else cart[id] = next;
  saveCart();
}

function renderCart() {
  const ids = Object.keys(cart).map(Number).filter((id) => products.some((p) => p.id === id));
  const count = ids.reduce((sum, id) => sum + cart[id], 0);
  const total = ids.reduce((sum, id) => sum + cart[id] * products.find((p) => p.id === id).price, 0);

  $("cartCount").textContent = count;
  $("cartTotal").textContent = rupee(total);
  $("placeBtn").disabled = count === 0;

  $("cartItems").innerHTML = ids.length
    ? ids.map((id) => {
        const p = products.find((x) => x.id === id);
        return `
        <div class="line-item">
          <div>
            <div class="name">${esc(p.name)}</div>
            <div class="sub">${rupee(p.price)} each</div>
            <div class="qty">
              <button data-dec="${id}" aria-label="Decrease quantity">−</button>
              <span>${cart[id]}</span>
              <button data-inc="${id}" aria-label="Increase quantity">+</button>
            </div>
          </div>
          <strong>${rupee(p.price * cart[id])}</strong>
        </div>`;
      }).join("")
    : `<p class="empty">Your cart is empty. Add something from the shop.</p>`;
}

function toggleCart(open) {
  $("drawer").hidden = !open;
  $("overlay").hidden = !open;
  if (open) $("formError").textContent = "";
}

/* ---------- checkout ---------- */
async function placeOrder(event) {
  event.preventDefault();
  const name = $("custName").value.trim();
  const email = $("custEmail").value.trim();
  const items = Object.entries(cart).map(([productId, quantity]) => ({ productId: Number(productId), quantity }));

  if (!name || !email.includes("@")) {
    $("formError").textContent = "Enter your name and a valid email.";
    return;
  }
  $("formError").textContent = "";
  $("placeBtn").disabled = true;

  try {
    const order = await api("/orders", {
      method: "POST",
      body: JSON.stringify({ customerName: name, customerEmail: email, items }),
    });
    cart = {};
    localStorage.setItem("cart", "{}");
    localStorage.setItem("email", email);
    toggleCart(false);
    toast("Order #" + order.id + " placed. Total " + rupee(order.total));
    await loadProducts(); // stock changed
    renderCart();
  } catch (e) {
    $("formError").textContent = e.message;
    $("placeBtn").disabled = false;
  }
}

/* ---------- orders ---------- */
async function loadOrders() {
  $("ordersStatus").textContent = "Loading...";
  try {
    const orders = await api("/orders");
    $("ordersStatus").textContent = orders.length ? "" : "No orders yet. Place your first order from the shop.";
    $("orders").innerHTML = orders.map((o) => `
      <article class="order">
        <header>
          <strong>Order #${o.id}</strong>
          <span>${new Date(o.createdAt).toLocaleString("en-IN")}</span>
          <strong>${rupee(o.total)}</strong>
        </header>
        <div>${esc(o.customerName)} (${esc(o.customerEmail)})</div>
        <ul>${o.items.map((i) => `<li>${esc(i.productName)} × ${i.quantity}</li>`).join("")}</ul>
      </article>`).join("");
  } catch (e) {
    $("ordersStatus").textContent = "Could not load orders: " + e.message;
  }
}

/* ---------- wiring ---------- */
function showView(name) {
  document.querySelectorAll(".view").forEach((v) => (v.hidden = v.id !== "view-" + name));
  document.querySelectorAll(".tab").forEach((t) => t.classList.toggle("is-active", t.dataset.view === name));
  if (name === "orders") loadOrders();
}

document.addEventListener("click", (e) => {
  const t = e.target;
  if (t.dataset.add) addToCart(Number(t.dataset.add));
  if (t.dataset.inc) changeQty(Number(t.dataset.inc), 1);
  if (t.dataset.dec) changeQty(Number(t.dataset.dec), -1);
  if (t.dataset.view) showView(t.dataset.view);
});
$("openCart").addEventListener("click", () => toggleCart(true));
$("closeCart").addEventListener("click", () => toggleCart(false));
$("overlay").addEventListener("click", () => toggleCart(false));
document.addEventListener("keydown", (e) => e.key === "Escape" && toggleCart(false));
$("checkoutForm").addEventListener("submit", placeOrder);

$("custEmail").value = localStorage.getItem("email") || "";
loadProducts().then(renderCart);
