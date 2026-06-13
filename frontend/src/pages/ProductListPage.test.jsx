import {
    cleanup,
    render,
    screen,
    waitFor
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
    MemoryRouter
} from "react-router-dom";
import {
    describe,
    expect,
    afterEach,
    it,
    vi
} from "vitest";
import ProductListPage from "./ProductListPage.jsx";
import {
    ApiError,
    fetchProducts
} from "../api/productApi.js";

vi.mock("../api/productApi.js", () => ({
    ApiError: class ApiError extends Error {
        constructor(response, body) {
            super(body?.message ?? `Request failed with status ${response.status}`);
            this.status = response.status;
        }
    },
    fetchProducts: vi.fn()
}));

describe("ProductListPage", () => {

    afterEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    it("renders products and applies filters", async () => {

        fetchProducts.mockResolvedValue(
            productPage([
                {
                    id: 1,
                    name: "Sky Blue Embroidered Lawn Suit",
                    category: "Lawn",
                    price: 7500,
                    imageUrl: "/products/Blue Lawn Suit.png",
                    estimatedDelivery: null,
                    estimatedDeliveryRange: {
                        from: "2026-06-18",
                        to: "2026-06-21"
                    },
                    availableSizes: ["M", "L"]
                }
            ])
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("Sky Blue Embroidered Lawn Suit")
        ).toBeTruthy();

        expect(
            screen.getByText("Delivery 18 Jun 2026 – 21 Jun 2026")
        ).toBeTruthy();

        const user = userEvent.setup();
        const filters = screen.getAllByRole("combobox");

        await user.selectOptions(filters[0], "Lawn");
        await user.selectOptions(filters[1], "M");
        await user.selectOptions(filters[2], "Lahore");
        await user.type(
            screen.getByPlaceholderText("Min Price"),
            "7000"
        );
        await user.type(
            screen.getByPlaceholderText("Max Price"),
            "8000"
        );
        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "Apply Filters"
                }
            )
        );

        await waitFor(() => {
            expect(fetchProducts).toHaveBeenLastCalledWith({
                category: "Lawn",
                size: "M",
                city: "Lahore",
                minPrice: "7000",
                maxPrice: "8000",
                page: 0,
                pageSize: 6
            });
        });

        expect(
            screen.getByRole(
                "link",
                {
                    name: /Sky Blue Embroidered Lawn Suit/i
                }
            ).getAttribute("href")
        ).toBe("/products/1?city=Lahore");
    });

    it("shows delivery estimate after deliver to city is selected", async () => {

        fetchProducts.mockResolvedValue(
            productPage([
                {
                    id: 1,
                    name: "Sky Blue Embroidered Lawn Suit",
                    category: "Lawn",
                    price: 7500,
                    imageUrl: "/products/Blue Lawn Suit.png",
                    estimatedDelivery: "2026-06-18",
                    availableSizes: ["M", "L"]
                }
            ])
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("Sky Blue Embroidered Lawn Suit")
        ).toBeTruthy();

        const user = userEvent.setup();
        const filters = screen.getAllByRole("combobox");

        await user.selectOptions(filters[2], "Lahore");
        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "Apply Filters"
                }
            )
        );

        expect(
            await screen.findByText("Delivery by 18 Jun 2026")
        ).toBeTruthy();
    });

    it("loads the next page", async () => {

        fetchProducts.mockResolvedValueOnce(
            productPage(
                [
                    {
                        id: 1,
                        name: "Sky Blue Embroidered Lawn Suit",
                        category: "Lawn",
                        price: 7500,
                        imageUrl: "/products/Blue Lawn Suit.png",
                        estimatedDelivery: null,
                        availableSizes: ["M", "L"]
                    }
                ],
                {
                    page: 0,
                    totalElements: 12,
                    totalPages: 2,
                    last: false
                }
            )
        );

        fetchProducts.mockResolvedValueOnce(
            productPage(
                [
                    {
                        id: 7,
                        name: "Formal Silk Suit",
                        category: "Formal",
                        price: 9500,
                        imageUrl: "/products/Maroon Formal Suit.png",
                        estimatedDelivery: null,
                        availableSizes: ["S"]
                    }
                ],
                {
                    page: 1,
                    totalElements: 12,
                    totalPages: 2,
                    last: true
                }
            )
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("Sky Blue Embroidered Lawn Suit")
        ).toBeTruthy();

        const user = userEvent.setup();

        await user.click(
            screen.getByRole(
                "button",
                {
                    name: "Next"
                }
            )
        );

        expect(
            await screen.findByText("Formal Silk Suit")
        ).toBeTruthy();

        expect(fetchProducts).toHaveBeenLastCalledWith({
            category: "",
            size: "",
            city: "",
            minPrice: "",
            maxPrice: "",
            page: 1,
            pageSize: 6
        });
    });

    it("restores the page from the URL so returning to the listing keeps the page", async () => {

        fetchProducts.mockResolvedValue(
            productPage(
                [
                    {
                        id: 7,
                        name: "Formal Silk Suit",
                        category: "Formal",
                        price: 9500,
                        imageUrl: "/products/Maroon Formal Suit.png",
                        estimatedDelivery: null,
                        availableSizes: ["S"]
                    }
                ],
                {
                    page: 1,
                    totalElements: 12,
                    totalPages: 2,
                    last: true
                }
            )
        );

        render(
            <MemoryRouter initialEntries={["/?page=1"]}>
                <ProductListPage />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("Formal Silk Suit")
        ).toBeTruthy();

        expect(fetchProducts).toHaveBeenCalledWith({
            category: "",
            size: "",
            city: "",
            minPrice: "",
            maxPrice: "",
            page: 1,
            pageSize: 6
        });
    });

    it("shows backend validation errors", async () => {

        fetchProducts.mockRejectedValue(
            new ApiError(
                {
                    status: 400
                },
                {
                    message: "minPrice cannot be greater than maxPrice"
                }
            )
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        const alert =
            await screen.findByRole("alert");

        expect(alert.textContent).toBe(
            "minPrice cannot be greater than maxPrice"
        );
    });

    it("shows generic server errors", async () => {

        fetchProducts.mockRejectedValue(
            new ApiError(
                {
                    status: 500
                },
                {
                    message: "Internal Server Error"
                }
            )
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        const alert =
            await screen.findByRole("alert");

        expect(alert.textContent).toBe(
            "Something went wrong while loading products."
        );
    });

    it("shows network errors", async () => {

        fetchProducts.mockRejectedValue(
            new TypeError("Failed to fetch")
        );

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        const alert =
            await screen.findByRole("alert");

        expect(alert.textContent).toBe(
            "We couldn’t load products right now. Please try again."
        );
    });
});

function productPage(
    content,
    overrides = {}
) {
    return {
        content,
        page: overrides.page ?? 0,
        size: overrides.size ?? 6,
        totalElements: overrides.totalElements ?? content.length,
        totalPages: overrides.totalPages ?? 1,
        last: overrides.last ?? true
    };
}
