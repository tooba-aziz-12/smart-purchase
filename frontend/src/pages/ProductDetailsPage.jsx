import { useEffect, useState } from "react";
import {useParams, Link, useNavigate} from "react-router-dom";

function ProductDetailsPage() {

    const { id } = useParams();

    const [product, setProduct] = useState(null);
    const [selectedSize, setSelectedSize] = useState("");
    const navigate = useNavigate();

    const [similarProducts, setSimilarProducts] = useState([]);

    useEffect(() => {

        fetch(`http://localhost:8095/products/${id}`)
            .then(response => response.json())
            .then(product => {

                setProduct(product);

                const maxPrice =
                    Number(product.price) + 1000;

                return fetch(
                    `http://localhost:8095/products?category=${product.category}&maxPrice=${maxPrice}`
                )
                    .then(response => response.json())
                    .then(products => {

                        setSimilarProducts(
                            products
                                .filter(
                                    p => p.id !== Number(id)
                                )
                                .sort(
                                    (a, b) =>
                                        Math.abs(a.price - product.price)
                                        -
                                        Math.abs(b.price - product.price)
                                )
                        );
                    });
            })
            .catch(console.error);

    }, [id]);

    const addToCart = () => {

        if (!selectedSize) {
            alert("Please select a size");
            return;
        }

        alert(
            `Added ${product.name} (${selectedSize}) to cart`
        );
    };

    if (!product) {
        return <div>Loading...</div>;
    }

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
                            gridTemplateColumns: "280px 1fr",
                            gap: "40px"
                        }}
                    >
                        <div>
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
                                        onClick={() =>
                                            setSelectedSize(sizeOption.size)
                                        }
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
                                        color: "#111827"
                                    }}
                                >
                                    {product.estimatedDelivery}
                                </div>
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

                        </div>
                    </div>
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

                                <div
                                    key={similarProduct.id}
                                    onClick={() =>
                                        navigate(
                                            `/products/${similarProduct.id}`
                                        )
                                    }
                                    style={{
                                        minWidth: "260px",
                                        background: "#f8fafc",
                                        border: "1px solid #e5e7eb",
                                        borderRadius: "12px",
                                        padding: "16px",
                                        cursor: "pointer",
                                        flexShrink: 0
                                    }}
                                >
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
                                        Delivery by {similarProduct.deliveryDate}
                                    </div>
                                </div>

                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetailsPage;