document.addEventListener('DOMContentLoaded', function () {
    const COLORS = [
        '#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1',
        '#fd7e14', '#20c997', '#0dcaf0', '#d63384', '#6c757d',
        '#0d6efd', '#198754', '#ffc107', '#dc3545', '#6f42c1'
    ];

    fetch('/api/portfolio/allocation')
        .then(r => r.json())
        .then(data => {
            renderChart('stockChart', data.bySymbol, 'doughnut');
            renderChart('sectorChart', data.bySector, 'pie');
        })
        .catch(err => console.warn('Chart load failed:', err));

    function renderChart(canvasId, dataMap, type) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;
        const labels = Object.keys(dataMap);
        const values = Object.values(dataMap).map(v => parseFloat(v));
        if (labels.length === 0) return;

        new Chart(canvas, {
            type: type,
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: COLORS.slice(0, labels.length),
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'right', labels: { boxWidth: 12, padding: 8, font: { size: 11 } } },
                    tooltip: {
                        callbacks: {
                            label: function (ctx) {
                                const val = ctx.parsed;
                                const total = ctx.dataset.data.reduce((a, b) => a + b, 0);
                                const pct = ((val / total) * 100).toFixed(1);
                                return ctx.label + ': ₹' + val.toLocaleString('en-IN') + ' (' + pct + '%)';
                            }
                        }
                    }
                }
            }
        });
    }

    // Auto-refresh page every 60 sec to see live prices
    setTimeout(() => location.reload(), 60_000);
});
