// PKR is shown in whole rupees with thousands separators, matching how
// Pakistani storefronts display prices (paisa is not used in retail).
const priceFormatter = new Intl.NumberFormat("en-PK", {
    maximumFractionDigits: 0
});

export function formatPrice(value) {
    const amount = typeof value === "number" ? value : Number(value);

    if (!Number.isFinite(amount)) {
        return "PKR —";
    }

    return `PKR ${priceFormatter.format(amount)}`;
}
