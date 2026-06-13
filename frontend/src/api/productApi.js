const BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8095";

export class ApiError extends Error {
    constructor(response, body) {
        super(body?.message ?? `Request failed with status ${response.status}`);
        this.name = "ApiError";
        this.status = response.status;
        this.body = body;
    }
}

async function requestJson(url) {

    const response = await fetch(url);

    const body = await response.json();

    if (!response.ok) {
        throw new ApiError(response, body);
    }

    return body;
}

export async function fetchProducts(filters) {

    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
        if (value !== "" && value !== null && value !== undefined) {
            params.append(key, value);
        }
    });

    return requestJson(
        `${BASE_URL}/products?${params.toString()}`
    );
}

export async function fetchProductDetails(id, options = {}) {

    const params = new URLSearchParams();

    if (options.city) {
        params.append("city", options.city);
    }

    const query = params.toString();
    const suffix = query ? `?${query}` : "";

    return requestJson(
        `${BASE_URL}/products/${id}${suffix}`
    );
}

export async function fetchSimilarProducts(id, options = {}) {

    const params = new URLSearchParams();

    if (options.city) {
        params.append("city", options.city);
    }

    const query = params.toString();
    const suffix = query ? `?${query}` : "";

    return requestJson(
        `${BASE_URL}/products/${id}/similar${suffix}`
    );
}