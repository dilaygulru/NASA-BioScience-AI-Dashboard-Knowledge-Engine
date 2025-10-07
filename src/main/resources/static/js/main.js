// 🌌 main.js — Astrogenesis Frontend Logic

console.log("🚀 Astrogenesis main.js loaded");

// Helper: simple fetch wrapper
async function apiFetch(url, options = {}) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return await res.json().catch(() => res.text());
    } catch (err) {
        console.error("API fetch error:", err);
        alert("An error occurred while communicating with the server.");
    }
}

/* ---------------------------------------------------
   🧠 AI SUMMARY + RECOMMENDATION
--------------------------------------------------- */
const aiForm = document.getElementById("aiForm");
if (aiForm) {
    aiForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const query = document.getElementById("query").value.trim();
        if (!query) return alert("Please enter a query.");

        const summaryContainer = document.getElementById("aiResult");
        summaryContainer.innerHTML = "<p>⏳ Generating summary...</p>";

        const res = await apiFetch(`/ai/summarize?query=${encodeURIComponent(query)}`);

        if (typeof res === "string") {
            summaryContainer.innerHTML = `<p>${res}</p>`;
        } else {
            summaryContainer.innerHTML = `<pre>${JSON.stringify(res, null, 2)}</pre>`;
        }

        // Enable PDF export button
        const pdfBtn = document.getElementById("generatePdfBtn");
        pdfBtn.removeAttribute("disabled");
        pdfBtn.dataset.query = query;
    });
}

// 🧾 PDF GENERATION
const pdfBtn = document.getElementById("generatePdfBtn");
if (pdfBtn) {
    pdfBtn.addEventListener("click", async () => {
        const query = pdfBtn.dataset.query;
        if (!query) return alert("Please generate a summary first.");

        const url = `/report/generate?query=${encodeURIComponent(query)}`;
        window.open(url, "_blank"); // Download PDF directly
    });
}

/* ---------------------------------------------------
   📚 NASA PUBLICATION VIEW / SEARCH
--------------------------------------------------- */
const researchTable = document.getElementById("researchTable");
if (researchTable) {
    async function loadPublications(query = "") {
        const endpoint = query
            ? `/publications/search?q=${encodeURIComponent(query)}`
            : `/publications/api`;
        const publications = await apiFetch(endpoint);

        const tbody = researchTable.querySelector("tbody");
        tbody.innerHTML = "";

        if (!publications || publications.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4">No results found.</td></tr>`;
            return;
        }

        publications.forEach((p) => {
            const row = document.createElement("tr");
            row.innerHTML = `
        <td>${p.id || "-"}</td>
        <td>${p.title || "Untitled"}</td>
        <td>${p.author || "Unknown"}</td>
        <td>${p.year || "-"}</td>
      `;
            tbody.appendChild(row);
        });
    }

    // Initial load
    loadPublications();

    // Search
    const searchForm = document.getElementById("searchForm");
    if (searchForm) {
        searchForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const query = document.getElementById("searchInput").value.trim();
            loadPublications(query);
        });
    }
}

/* ---------------------------------------------------
   🕓 REPORT HISTORY (optional)
--------------------------------------------------- */
const reportHistory = document.getElementById("reportHistory");
if (reportHistory) {
    async function loadReports() {
        const reports = await apiFetch("/report/history");
        if (!reports || reports.length === 0) {
            reportHistory.innerHTML = "<p>No previous reports found.</p>";
            return;
        }

        reportHistory.innerHTML = `
      <ul>
        ${reports
            .map(
                (r) => `<li>${r.query} - <a href="/report/view/${r.id}">View</a></li>`
            )
            .join("")}
      </ul>`;
    }

    loadReports();
}

