import {
    afterEach,
    beforeEach,
    describe,
    expect,
    it,
    vi
} from "vitest";
import {
    ApiError,
    fetchProductDetails,
    fetchProducts,
    fetchSimilarProducts
} from "./productApi.js";

describe("productApi", () => {

    beforeEach(() => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            status: 200,
            json: vi.fn().mockResolvedValue([])
        });
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it("fetches products with non-empty filters", async () => {

        await fetchProducts({
            category: "Lawn",
            size: "",
            city: "Lahore",
            minPrice: "7000",
            maxPrice: null
        });

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "http://localhost:8095/products?category=Lawn&city=Lahore&minPrice=7000"
        );
    });

    it("fetches product details by id", async () => {

        await fetchProductDetails(1);

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "http://localhost:8095/products/1"
        );
    });

    it("fetches similar products by id", async () => {

        await fetchSimilarProducts(1);

        expect(globalThis.fetch).toHaveBeenCalledWith(
            "http://localhost:8095/products/1/similar"
        );
    });

    it("throws ApiError for non-success responses", async () => {

        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false,
            status: 404,
            json: vi.fn().mockResolvedValue({
                message: "Product not found"
            })
        });

        await expect(
            fetchProductDetails(999)
        ).rejects.toBeInstanceOf(ApiError);
    });
});
