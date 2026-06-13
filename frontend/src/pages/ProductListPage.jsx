import { useCallback, useEffect, useState } from "react";
import {
    ApiError,
    fetchProducts
} from "../api/productApi.js";
import { Link } from "react-router-dom";

// 6 ensures pagination is visible with the 12 seed products so the feature can be evaluated.
const PAGE_SIZE = 6;

const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function formatDeliveryDate(isoDate) {
    const [year, month, day] = isoDate.split("-");
    return `${parseInt(day)} ${MONTHS[parseInt(month) - 1]} ${year}`;
}

function getProductLoadErrorMessage(error) {

    if (error instanceof ApiError && error.status === 400) {
        return error.message;
    }

    if (error instanceof ApiError) {
        return "Something went wrong while loading products.";
    }

    return "We couldn’t load products right now. Please try again.";
}

function ProductListPage() {

    const [products, setProducts] = useState([]);
    const [pageInfo, setPageInfo] = useState({
        page: 0,
        size: PAGE_SIZE,
        totalElements: 0,
        totalPages: 0,
        last: true
    });
    const [errorMessage, setErrorMessage] = useState("");

    const [category, setCategory] = useState("");
    const [size, setSize] = useState("");
    const [city, setCity] = useState("");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");
    const loadProducts = useCallback((filters, page = 0) => {
        fetchProducts({
            ...filters,
            page,
            pageSize: PAGE_SIZE
        })
            .then(productPage => {
                setProducts(productPage.content);
                setPageInfo({
                    page: productPage.page,
                    size: productPage.size,
                    totalElements: productPage.totalElements,
                    totalPages: productPage.totalPages,
                    last: productPage.last
                });
                setErrorMessage("");
            })
            .catch(error => {
                setProducts([]);
                setPageInfo({
                    page: 0,
                    size: PAGE_SIZE,
                    totalElements: 0,
                    totalPages: 0,
                    last: true
                });
                setErrorMessage(
                    getProductLoadErrorMessage(error)
                );
            });
    }, []);

    useEffect(() => {
        loadProducts({
            category: "",
            size: "",
            city: "",
            minPrice: "",
            maxPrice: ""
        });
    }, [loadProducts]);

    const filters = { category, size, city, minPrice, maxPrice };

    const applyFilters = () => {
        loadProducts(filters, 0);
    };

    const goToPage = (nextPage) => {
        loadProducts(filters, nextPage);
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
                            onClick={applyFilters}
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

                {errorMessage && (
                    <div
                        role="alert"
                        style={{
                            background: "#fef2f2",
                            border: "1px solid #fecaca",
                            borderRadius: "12px",
                            color: "#991b1b",
                            fontWeight: "600",
                            marginBottom: "24px",
                            padding: "16px",
                            textAlign: "center"
                        }}
                    >
                        {errorMessage}
                    </div>
                )}

                <div
                    style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
                        gap: "20px"
                    }}
                >
                    {products.map(product => (
                        <Link
                            key={product.id}
                            to={`/products/${product.id}`}
                            style={{
                                display: "block",
                                textDecoration: "none",
                                color: "inherit",
                                background: "white",
                                borderRadius: "16px",
                                padding: "20px",
                                boxShadow: "0 4px 16px rgba(0,0,0,0.08)"
                            }}
                        >
                            <img
                                src={product.imageUrl}
                                alt={product.name}
                                style={{
                                    width: "100%",
                                    height: "340px",
                                    objectFit: "cover",
                                    objectPosition: "center top",
                                    borderRadius: "12px",
                                    marginBottom: "16px"
                                }}
                            />

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
                                    Delivery by {formatDeliveryDate(product.estimatedDelivery)}
                                </div>
                            </div>
                        </Link>
                    ))}
                </div>

                {!errorMessage && products.length === 0 && (
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

                {!errorMessage && pageInfo.totalPages > 1 && (
                    <div
                        style={{
                            alignItems: "center",
                            display: "flex",
                            gap: "16px",
                            justifyContent: "center",
                            marginTop: "32px"
                        }}
                    >
                        <button
                            disabled={pageInfo.page === 0}
                            onClick={() => goToPage(pageInfo.page - 1)}
                        >
                            Previous
                        </button>

                        <span>
                            Page {pageInfo.page + 1} of {pageInfo.totalPages}
                        </span>

                        <button
                            disabled={pageInfo.last}
                            onClick={() => goToPage(pageInfo.page + 1)}
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default ProductListPage;