import {
    cleanup,
    render,
    screen
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
    MemoryRouter,
    Route,
    Routes
} from "react-router-dom";
import {
    afterEach,
    describe,
    expect,
    it,
    vi
} from "vitest";
import ProductDetailsPage from "./ProductDetailsPage.jsx";
import {
    ApiError,
    fetchProductDetails,
    fetchSimilarProducts
} from "../api/productApi.js";

vi.mock("../api/productApi.js", () => ({
    ApiError: class ApiError extends Error {
        constructor(response, body) {
            super(body?.message ?? `Request failed with status ${response.status}`);
            this.status = response.status;
        }
    },
    fetchProductDetails: vi.fn(),
    fetchSimilarProducts: vi.fn()
}));

describe("ProductDetailsPage", () => {

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
        vi.restoreAllMocks();
    });

    it("renders details, price breakdown, selectable size, and similar products", async () => {

        fetchProductDetails.mockResolvedValue({
            id: 1,
            name: "Blue Lawn Suit",
            category: "Lawn",
            price: 7500,
            estimatedDelivery: "2026-06-18",
            sizes: [
                {
                    size: "S",
                    available: false
                },
                {
                    size: "M",
                    available: true
                },
                {
                    size: "L",
                    available: true
                }
            ],
            priceBreakdown: {
                productPrice: 7500,
                platformFee: 200,
                deliveryFee: 250,
                vat: 1192.50,
                total: 9142.50
            }
        });

        fetchSimilarProducts.mockResolvedValue([
            {
                id: 2,
                name: "Green Lawn Suit",
                category: "Lawn",
                price: 6900,
                estimatedDelivery: "2026-06-18"
            }
        ]);

        render(
            <MemoryRouter initialEntries={["/products/1"]}>
                <Routes>
                    <Route
                        path="/products/:id"
                        element={<ProductDetailsPage />}
                    />
                </Routes>
            </MemoryRouter>
        );

        expect(
            await screen.findByRole(
                "heading",
                {
                    name: "Blue Lawn Suit"
                }
            )
        ).toBeTruthy();

        expect(screen.getByText("PKR 7500")).toBeTruthy();
        expect(screen.getByText("PKR 200")).toBeTruthy();
        expect(screen.getByText("PKR 250")).toBeTruthy();
        expect(screen.getByText("PKR 1192.5")).toBeTruthy();
        expect(screen.getByText("PKR 9142.5")).toBeTruthy();
        expect(screen.getByText("Green Lawn Suit")).toBeTruthy();

        expect(
            screen.getByRole(
                "button",
                {
                    name: "S"
                }
            ).disabled
        ).toBe(true);

        const user = userEvent.setup();

        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "Add To Cart"
                }
            )
        );

        expect(
            screen.getByRole("status").textContent
        ).toBe("Please select a size");

        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "M"
                }
            )
        );
        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "Add To Cart"
                }
            )
        );

        expect(
            screen.getByRole("status").textContent
        ).toBe(
            "Added Blue Lawn Suit (M) to cart"
        );
    });

    it("hides similar products section when there are no recommendations", async () => {

        fetchProductDetails.mockResolvedValue({
            id: 6,
            name: "Maroon Formal Suit",
            category: "Formal",
            price: 12500,
            estimatedDelivery: "2026-06-18",
            sizes: [
                {
                    size: "S",
                    available: false
                },
                {
                    size: "M",
                    available: false
                },
                {
                    size: "L",
                    available: true
                }
            ],
            priceBreakdown: {
                productPrice: 12500,
                platformFee: 200,
                deliveryFee: 250,
                vat: 1942.50,
                total: 14892.50
            }
        });

        fetchSimilarProducts.mockResolvedValue([]);

        render(
            <MemoryRouter initialEntries={["/products/6"]}>
                <Routes>
                    <Route
                        path="/products/:id"
                        element={<ProductDetailsPage />}
                    />
                </Routes>
            </MemoryRouter>
        );

        expect(
            await screen.findByRole(
                "heading",
                {
                    name: "Maroon Formal Suit"
                }
            )
        ).toBeTruthy();

        expect(
            screen.queryByRole(
                "heading",
                {
                    name: "You May Also Like"
                }
            )
        ).toBeNull();
    });

    it("shows an error when product details cannot be loaded", async () => {

        fetchProductDetails.mockRejectedValue(
            new ApiError(
                {
                    status: 404
                },
                {
                    message: "Product not found"
                }
            )
        );

        render(
            <MemoryRouter initialEntries={["/products/999"]}>
                <Routes>
                    <Route
                        path="/products/:id"
                        element={<ProductDetailsPage />}
                    />
                </Routes>
            </MemoryRouter>
        );

        const alert =
            await screen.findByRole("alert");

        expect(alert.textContent).toBe("Product not found.");
    });
});
