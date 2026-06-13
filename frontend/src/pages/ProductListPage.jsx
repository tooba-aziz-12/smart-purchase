import { useEffect, useState } from "react";
import { fetchProducts } from "../api/productApi.js";
import { useNavigate } from "react-router-dom";

function ProductListPage() {

    const [products, setProducts] = useState([]);

    const [category, setCategory] = useState("");
    const [size, setSize] = useState("");
    const [city, setCity] = useState("");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        loadProducts();
    }, []);

    const loadProducts = () => {
        fetchProducts({
            category,
            size,
            city,
            minPrice,
            maxPrice
        })
            .then(setProducts)
            .catch(console.error);
    };

    return (
        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#f5f7fb",
                padding: "40px",
                fontFamily:
                    "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif"
            }}
        >
            <div
                style={{
                    maxWidth: "1200px",
                    margin: "0 auto"
                }}
            >
                <h1
                    style={{
                        textAlign: "center",
                        fontSize: "48px",
                        marginBottom: "8px",
                        color: "#111827"
                    }}
                >
                    Smart Purchase
                </h1>

                <p
                    style={{
                        textAlign: "center",
                        color: "#6b7280",
                        marginBottom: "40px"
                    }}
                >
                    Find products available across warehouses
                </p>

                <div
                    style={{
                        background: "white",
                        borderRadius: "16px",
                        padding: "24px",
                        boxShadow: "0 4px 20px rgba(0,0,0,0.08)",
                        marginBottom: "32px"
                    }}
                >
                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(5, 1fr)",
                            gap: "16px"
                        }}
                    >
                        <select
                            value={category}
                            onChange={(e) => setCategory(e.target.value)}
                        >
                            <option value="">All Categories</option>
                            <option value="Lawn">Lawn</option>
                            <option value="Festive">Festive</option>
                            <option value="Formal">Formal</option>
                            <option value="Casual">Casual</option>
                        </select>

                        <select
                            value={size}
                            onChange={(e) => setSize(e.target.value)}
                        >
                            <option value="">All Sizes</option>
                            <option value="S">Small</option>
                            <option value="M">Medium</option>
                            <option value="L">Large</option>
                        </select>

                        <select
                            value={city}
                            onChange={(e) => setCity(e.target.value)}
                        >
                            <option value="">All Cities</option>
                            <option value="Karachi">Karachi</option>
                            <option value="Lahore">Lahore</option>
                            <option value="Islamabad">Islamabad</option>
                        </select>

                        <input
                            type="number"
                            placeholder="Min Price"
                            value={minPrice}
                            onChange={(e) => setMinPrice(e.target.value)}
                        />

                        <input
                            type="number"
                            placeholder="Max Price"
                            value={maxPrice}
                            onChange={(e) => setMaxPrice(e.target.value)}
                        />
                    </div>

                    <div
                        style={{
                            display: "flex",
                            justifyContent: "center",
                            marginTop: "20px"
                        }}
                    >
                        <button
                            onClick={loadProducts}
                            style={{
                                backgroundColor: "#1f3a5f",
                                color: "white",
                                border: "none",
                                borderRadius: "8px",
                                padding: "12px 24px",
                                cursor: "pointer",
                                fontWeight: "600"
                            }}
                        >
                            Apply Filters
                        </button>
                    </div>
                </div>

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
                        gap: "20px"
                    }}
                >
                    {products.map(product => (
                        <div
                            key={product.id}
                            onClick={() => navigate(`/products/${product.id}`)}
                            style={{
                                background: "white",
                                borderRadius: "16px",
                                padding: "20px",
                                boxShadow: "0 4px 16px rgba(0,0,0,0.08)",
                                cursor: "pointer"
                            }}
                        >
                            <div
                                style={{
                                    fontSize: "12px",
                                    color: "#365a80",
                                    fontWeight: "600",
                                    marginBottom: "10px",
                                    textTransform: "uppercase",
                                    letterSpacing: "0.5px"
                                }}
                            >
                                {product.category}
                            </div>

                            <h3
                                style={{
                                    margin: 0,
                                    marginBottom: "16px",
                                    color: "#111827",
                                    fontSize: "28px",
                                    fontWeight: "600"
                                }}
                            >
                                {product.name}
                            </h3>

                            <div
                                style={{
                                    display: "flex",
                                    gap: "8px",
                                    flexWrap: "wrap",
                                    marginBottom: "20px"
                                }}
                            >
                                {product.availableSizes?.map(size => (
                                    <span
                                        key={size}
                                        style={{
                                            backgroundColor: "#e8eef5",
                                            color: "#365a80",
                                            padding: "6px 12px",
                                            borderRadius: "999px",
                                            fontSize: "12px",
                                            fontWeight: "600"
                                        }}
                                    >
                    {size}
                  </span>
                                ))}
                            </div>

                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    alignItems: "flex-start",
                                    gap: "4px"
                                }}
                            >
                                <div
                                    style={{
                                        color: "#1f3a5f",
                                        fontSize: "14px",
                                        fontWeight: "600"
                                    }}
                                >
                                    PKR {product.price}
                                </div>

                                <div
                                    style={{
                                        color: "#6b7280",
                                        fontSize: "14px",
                                        fontWeight: "500"
                                    }}
                                >
                                    Delivery by {product.estimatedDelivery}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>

                {products.length === 0 && (
                    <div
                        style={{
                            textAlign: "center",
                            marginTop: "40px",
                            color: "#6b7280"
                        }}
                    >
                        No products found.
                    </div>
                )}
            </div>
        </div>
    );
}

export default ProductListPage;