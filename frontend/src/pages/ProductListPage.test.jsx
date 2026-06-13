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

        fetchProducts.mockResolvedValue([
            {
                id: 1,
                name: "Blue Lawn Suit",
                category: "Lawn",
                price: 7500,
                estimatedDelivery: "2026-06-18",
                availableSizes: ["M", "L"]
            }
        ]);

        render(
            <MemoryRouter>
                <ProductListPage />
            </MemoryRouter>
        );

        expect(
            await screen.findByText("Blue Lawn Suit")
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
                maxPrice: "8000"
            });
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
