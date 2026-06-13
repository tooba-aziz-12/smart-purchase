import { useEffect, useState } from "react";
import { useParams, Link, useSearchParams } from "react-router-dom";
import {
    ApiError,
    fetchProductDetails,
    fetchSimilarProducts
} from "../api/productApi.js";

import {
    renderDeliveryEstimate
} from "../utils/deliveryEstimate.js";

function buildProductPath(productId, city) {
    if (!city) {
        return `/products/${productId}`;
    }

    const params = new URLSearchParams({ city });
    return `/products/${productId}?${params.toString()}`;
}

function getProductDetailsErrorMessage(error) {

    if (error instanceof ApiError && error.status === 404) {
        return "Product not found.";
    }

    if (error instanceof ApiError) {
        return "Something went wrong while loading product details.";
    }

    return "We couldn’t load product details right now. Please try again.";
}

function ProductDetailsPage() {

    const { id } = useParams();
    const [searchParams] = useSearchParams();
    const city = searchParams.get("city") ?? "";

    const [product, setProduct] = useState(null);
    const [selectedSize, setSelectedSize] = useState("");
    const [errorMessage, setErrorMessage] = useState("");
    const [cartFeedback, setCartFeedback] = useState("");

    const [similarProducts, setSimilarProducts] = useState([]);

    useEffect(() => {

        Promise.all([
            fetchProductDetails(id, { city: city || undefined }),
            fetchSimilarProducts(id, { city: city || undefined }).catch(() => [])
        ])
            .then(([product, similar]) => {
                setProduct(product);
                setSelectedSize("");
                setErrorMessage("");
                setCartFeedback("");
                setSimilarProducts(similar);
            })
            .catch(error => {
                setProduct(null);
                setSimilarProducts([]);
                setErrorMessage(
                    getProductDetailsErrorMessage(error)
                );
            });

    }, [id, city]);

    const addToCart = () => {

        if (!selectedSize) {
            setCartFeedback("Please select a size");
            return;
        }

        setCartFeedback(
            `Added ${product.name} (${selectedSize}) to cart`
        );
    };

    if (errorMessage) {
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
                        maxWidth: "1000px",
                        margin: "0 auto"
                    }}
                >
                    <Link
                        to="/"
                        style={{
                            textDecoration: "none",
                            color: "#365a80",
                            fontWeight: "600"
                        }}
                    >
                        ← Back to Products
                    </Link>

                    <div
                        role="alert"
                        style={{
                            background: "#fef2f2",
                            border: "1px solid #fecaca",
                            borderRadius: "12px",
                            color: "#991b1b",
                            fontWeight: "600",
                            marginTop: "24px",
                            padding: "16px",
                            textAlign: "center"
                        }}
                    >
                        {errorMessage}
                    </div>
                </div>
            </div>
        );
    }

    if (!product) {
        return <div>Loading...</div>;
    }

    const selectedSizeOption = product.sizes.find(
        sizeOption => sizeOption.size === selectedSize
    );

    const displayedDelivery = {
        estimatedDelivery:
            selectedSizeOption?.estimatedDelivery ?? product.estimatedDelivery,
        estimatedDeliveryRange:
            selectedSizeOption?.estimatedDeliveryRange ??
            product.estimatedDeliveryRange
    };

    const hasExactDelivery = Boolean(displayedDelivery.estimatedDelivery);

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
                    maxWidth: "1000px",
                    margin: "0 auto"
                }}
            >
                <Link
                    to="/"
                    style={{
                        textDecoration: "none",
                        color: "#365a80",
                        fontWeight: "600"
                    }}
                >
                    ← Back to Products
                </Link>

                <div
                    style={{
                        marginTop: "20px",
                        background: "white",
                        borderRadius: "16px",
                        padding: "32px",
                        boxShadow: "0 4px 20px rgba(0,0,0,0.08)"
                    }}
                >
                    <div
                        style={{
                            fontSize: "12px",
                            color: "#365a80",
                            fontWeight: "600",
                            textTransform: "uppercase",
                            marginBottom: "12px"
                        }}
                    >
                        {product.category}
                    </div>

                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns: "360px 1fr",
                            gap: "40px"
                        }}
                    >
                        <div>
                            <img
                                src={product.imageUrl}
                                alt={product.name}
                                style={{
                                    width: "100%",
                                    height: "520px",
                                    objectFit: "cover",
                                    objectPosition: "center top",
                                    borderRadius: "12px",
                                    marginBottom: "24px"
                                }}
                            />

                            <div
                                style={{
                                    fontWeight: "600",
                                    marginBottom: "16px"
                                }}
                            >
                                Select Size
                            </div>

                            <div
                                style={{
                                    display: "flex",
                                    gap: "12px",
                                    marginBottom: "40px"
                                }}
                            >
                                {product.sizes.map(sizeOption => (
                                    <button
                                        key={sizeOption.size}
                                        disabled={!sizeOption.available}
                                        onClick={() => {
                                            setSelectedSize(sizeOption.size);
                                            setCartFeedback("");
                                        }}
                                        style={{
                                            width: "50px",
                                            height: "50px",
                                            borderRadius: "8px",
                                            border:
                                                selectedSize === sizeOption.size
                                                    ? "2px solid #1f3a5f"
                                                    : "1px solid #d1d5db",
                                            backgroundColor:
                                                !sizeOption.available
                                                    ? "#e5e7eb"
                                                    : selectedSize === sizeOption.size
                                                        ? "#e8eef5"
                                                        : "white",
                                            cursor:
                                                sizeOption.available
                                                    ? "pointer"
                                                    : "not-allowed"
                                        }}
                                    >
                                        {sizeOption.size}
                                    </button>
                                ))}
                            </div>

                            <div>
                                <div
                                    style={{
                                        color: "#6b7280",
                                        fontSize: "14px",
                                        marginBottom: "8px"
                                    }}
                                >
                                    Estimated Delivery
                                </div>

                                <div
                                    style={{
                                        fontWeight: "600",
                                        color: hasExactDelivery ? "#111827" : "#6b7280"
                                    }}
                                >
                                    {renderDeliveryEstimate(displayedDelivery)}
                                </div>
                                {city && selectedSize && hasExactDelivery && (
                                    <div
                                        style={{
                                            color: "#6b7280",
                                            fontSize: "13px",
                                            marginTop: "8px"
                                        }}
                                    >
                                        Delivery estimate for size {selectedSize} to {city}
                                    </div>
                                )}
                            </div>
                        </div>

                        <div>
                            <h1
                                style={{
                                    marginTop: 0,
                                    marginBottom: "12px",
                                    fontSize: "56px",
                                    lineHeight: "1.1"
                                }}
                            >
                                {product.name}
                            </h1>

                            <div
                                style={{
                                    borderTop: "1px solid #e5e7eb",
                                    paddingTop: "24px",
                                    marginTop: "24px",
                                    marginBottom: "32px"
                                }}
                            >
                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        marginBottom: "12px"
                                    }}
                                >
                                    <span>Product Price</span>
                                    <span>
                                        PKR {product.priceBreakdown.productPrice}
                                    </span>
                                </div>

                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        marginBottom: "12px"
                                    }}
                                >
                                    <span>Platform Fee</span>
                                    <span>
                                        PKR {product.priceBreakdown.platformFee}
                                    </span>
                                </div>

                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        marginBottom: "12px"
                                    }}
                                >
                                    <span>Standard Delivery Fee</span>
                                    <span>
                                        PKR {product.priceBreakdown.deliveryFee}
                                    </span>
                                </div>

                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        marginBottom: "12px"
                                    }}
                                >
                                    <span>VAT</span>
                                    <span>
                                        PKR {product.priceBreakdown.vat}
                                    </span>
                                </div>

                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        marginTop: "16px",
                                        paddingTop: "16px",
                                        borderTop: "1px solid #e5e7eb",
                                        fontWeight: "700",
                                        fontSize: "18px"
                                    }}
                                >
                                    <span>Total</span>
                                    <span>
                                        PKR {product.priceBreakdown.total}
                                    </span>
                                </div>
                            </div>

                            <button
                                onClick={addToCart}
                                style={{
                                    width: "100%",
                                    backgroundColor: "#1f3a5f",
                                    color: "white",
                                    border: "none",
                                    borderRadius: "8px",
                                    padding: "16px",
                                    fontSize: "16px",
                                    fontWeight: "600",
                                    cursor: "pointer"
                                }}
                            >
                                Add To Cart
                            </button>

                            {cartFeedback && (
                                <div
                                    role="status"
                                    style={{
                                        color:
                                            selectedSize
                                                ? "#166534"
                                                : "#991b1b",
                                        fontWeight: "600",
                                        marginTop: "12px",
                                        textAlign: "center"
                                    }}
                                >
                                    {cartFeedback}
                                </div>
                            )}

                        </div>
                    </div>
                    {similarProducts.length > 0 && (
                        <div
                            style={{
                                marginTop: "32px",
                                background: "white",
                                borderRadius: "16px",
                                padding: "24px",
                                boxShadow: "0 4px 20px rgba(0,0,0,0.08)"
                            }}
                        >
                            <h2
                                style={{
                                    marginTop: 0,
                                    marginBottom: "20px",
                                    fontSize: "24px",
                                    color: "#111827"
                                }}
                            >
                                You May Also Like
                            </h2>

                            <div
                                style={{
                                    display: "flex",
                                    gap: "16px",
                                    overflowX: "auto",
                                    paddingBottom: "8px"
                                }}
                            >
                                {similarProducts.map(similarProduct => (

                                    <Link
                                        key={similarProduct.id}
                                        to={buildProductPath(similarProduct.id, city)}
                                        style={{
                                            display: "block",
                                            textDecoration: "none",
                                            color: "inherit",
                                            minWidth: "260px",
                                            background: "#f8fafc",
                                            border: "1px solid #e5e7eb",
                                            borderRadius: "12px",
                                            padding: "16px",
                                            flexShrink: 0
                                        }}
                                    >
                                        <img
                                            src={similarProduct.imageUrl}
                                            alt={similarProduct.name}
                                            style={{
                                                width: "100%",
                                                height: "260px",
                                                objectFit: "cover",
                                                objectPosition: "center top",
                                                borderRadius: "10px",
                                                marginBottom: "12px"
                                            }}
                                        />

                                        <div
                                            style={{
                                                fontSize: "12px",
                                                color: "#365a80",
                                                textTransform: "uppercase",
                                                fontWeight: "600",
                                                marginBottom: "8px"
                                            }}
                                        >
                                            {similarProduct.category}
                                        </div>

                                        <div
                                            style={{
                                                fontWeight: "700",
                                                fontSize: "18px",
                                                color: "#111827",
                                                marginBottom: "12px"
                                            }}
                                        >
                                            {similarProduct.name}
                                        </div>

                                        <div
                                            style={{
                                                color: "#1f3a5f",
                                                fontWeight: "600",
                                                marginBottom: "6px"
                                            }}
                                        >
                                            PKR {similarProduct.price}
                                        </div>

                                        <div
                                            style={{
                                                color: "#6b7280",
                                                fontSize: "14px"
                                            }}
                                        >
                                        {renderDeliveryEstimate({
                                            estimatedDelivery: similarProduct.estimatedDelivery,
                                            estimatedDeliveryRange: similarProduct.estimatedDeliveryRange
                                        })}
                                        </div>
                                    </Link>

                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ProductDetailsPage;