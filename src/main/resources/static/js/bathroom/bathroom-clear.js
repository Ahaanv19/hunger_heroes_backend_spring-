async function clearTinkle() {
    const response = await fetch("/api/tinkle/bulk/clear", {  
        method: "POST",
        cache: "no-cache",
        headers: {
            "content-type": "application/json"
        }
    });

    if (response.ok) {
        const data = await response.json();
        alert(data.message || "All bathroom records have been cleared.");
        location.reload();
    } else {
        let errorMsg = "Failed to clear records.";
        try {
            const errorData = await response.json();
            errorMsg = errorData.message || errorMsg;
        } catch {}
        alert(errorMsg);
    }
}

document.getElementById("clear-all").addEventListener("click", async () => {
    if (confirm("Are you sure you want to clear all bathroom records? This action cannot be undone.")) {
        await clearTinkle();
    }
});