let usageChart = null;
let costPieChart = null;
let usersData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadDashboardStats();
    loadUsers();
    loadCostBreakdown();
    checkBotHealth();
});

function switchTab(tabId) {
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));

    const tabLink = document.querySelector(`a[href="#${tabId}"]`);
    if (tabLink) tabLink.classList.add('active');

    const pane = document.getElementById(`tab-${tabId}`);
    if (pane) pane.classList.add('active');

    const titles = {
        'dashboard': 'Dashboard Overview',
        'users': 'User Management',
        'broadcast': 'Broadcast Announcements',
        'costs': 'AI Costs & Models',
        'settings': 'Server Configuration'
    };
    document.getElementById('page-title').innerText = titles[tabId] || 'Admin Console';
}

async function fetchWithAuth(url, options = {}) {
    // Default HTTP Basic auth for demo or standard session
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + btoa('admin:admin123456'),
        ...options.headers
    };
    try {
        const res = await fetch(url, { ...options, headers });
        if (!res.ok) {
            throw new Error(`HTTP ${res.status}: ${res.statusText}`);
        }
        return await res.json();
    } catch (e) {
        console.error('API Error:', e);
        throw e;
    }
}

async function loadDashboardStats() {
    try {
        const stats = await fetchWithAuth('/api/admin/stats');
        document.getElementById('val-total-users').innerText = stats.totalUsers || 0;
        document.getElementById('val-active-users').innerText = `${stats.activeUsersToday || 0} Active today`;
        document.getElementById('val-messages-today').innerText = stats.messagesToday || 0;
        document.getElementById('val-total-messages').innerText = `${stats.totalMessages || 0} Total messages`;
        document.getElementById('val-ai-requests-today').innerText = stats.aiRequestsToday || 0;
        document.getElementById('val-tokens-today').innerText = `${(stats.totalTokensUsedToday || 0).toLocaleString()} Tokens`;
        document.getElementById('val-cost-today').innerText = `$${Number(stats.estimatedCostToday || 0).toFixed(4)}`;
        document.getElementById('val-cost-total').innerText = `Total: $${Number(stats.estimatedCostTotal || 0).toFixed(4)}`;

        renderUsageChart(stats.dailyUsageHistory || []);
        renderCostPieChart(stats.costBreakdown || []);
        showToast('Dashboard stats updated');
    } catch (e) {
        console.warn('Could not load live stats, using fallback:', e.message);
    }
}

function renderUsageChart(history) {
    const ctx = document.getElementById('usageChart').getContext('2d');
    const labels = history.length ? history.map(h => h.date) : ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const requestData = history.length ? history.map(h => h.requests) : [5, 12, 18, 24, 30, 42, 55];

    if (usageChart) usageChart.destroy();

    usageChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'AI Requests',
                data: requestData,
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { labels: { color: '#94a3b8' } }
            },
            scales: {
                x: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
                y: { ticks: { color: '#94a3b8' }, grid: { color: 'rgba(255,255,255,0.05)' } }
            }
        }
    });
}

function renderCostPieChart(costBreakdown) {
    const ctx = document.getElementById('costPieChart').getContext('2d');
    const labels = costBreakdown.length ? costBreakdown.map(c => c.model) : ['gpt-4o-mini', 'gpt-4o'];
    const data = costBreakdown.length ? costBreakdown.map(c => c.estimatedCost) : [0.05, 0.20];

    if (costPieChart) costPieChart.destroy();

    costPieChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b']
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom', labels: { color: '#94a3b8' } }
            }
        }
    });
}

async function loadUsers() {
    try {
        const page = await fetchWithAuth('/api/admin/users?page=0&size=50');
        usersData = page.content || [];
        renderUsersTable(usersData);
    } catch (e) {
        document.getElementById('usersTableBody').innerHTML = '<tr><td colspan="7" class="text-center">No users registered yet.</td></tr>';
    }
}

function renderUsersTable(users) {
    const tbody = document.getElementById('usersTableBody');
    if (!users.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">No users found.</td></tr>';
        return;
    }

    tbody.innerHTML = users.map(u => {
        const statusBadge = u.status === 'ACTIVE'
            ? '<span class="badge badge-success">ACTIVE</span>'
            : '<span class="badge badge-danger">BLOCKED</span>';

        const tierBadge = `<span class="badge badge-primary">${u.tier}</span>`;
        const actionBtn = u.status === 'ACTIVE'
            ? `<button class="btn btn-sm btn-outline" style="color:#ef4444;" onclick="toggleUserStatus(${u.id})">Block</button>`
            : `<button class="btn btn-sm btn-outline" style="color:#10b981;" onclick="toggleUserStatus(${u.id})">Unblock</button>`;

        const tierBtn = u.tier === 'FREE'
            ? `<button class="btn btn-sm btn-outline" style="color:#3b82f6;" onclick="changeTier(${u.id}, 'PREMIUM')">Upgrade</button>`
            : `<button class="btn btn-sm btn-outline" style="color:#94a3b8;" onclick="changeTier(${u.id}, 'FREE')">Downgrade</button>`;

        return `
            <tr>
                <td>#${u.id}</td>
                <td><strong>${u.firstName || ''} ${u.lastName || ''}</strong> (@${u.username || 'n/a'})<br><small style="color:#64748b">TG: ${u.telegramUserId}</small></td>
                <td>${u.language || 'en'}</td>
                <td>${tierBadge}</td>
                <td>${statusBadge}</td>
                <td>${new Date(u.createdAt).toLocaleDateString()}</td>
                <td>
                    <div style="display:flex; gap:6px;">
                        ${actionBtn}
                        ${tierBtn}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function filterUsersTable() {
    const query = document.getElementById('userSearchInput').value.toLowerCase();
    const filtered = usersData.filter(u => 
        (u.username && u.username.toLowerCase().includes(query)) ||
        (u.firstName && u.firstName.toLowerCase().includes(query)) ||
        (u.telegramUserId && u.telegramUserId.toString().includes(query))
    );
    renderUsersTable(filtered);
}

async function toggleUserStatus(userId) {
    try {
        await fetchWithAuth(`/api/admin/users/${userId}/status`, { method: 'PUT' });
        showToast('User status updated');
        loadUsers();
    } catch (e) {
        showToast('Failed to update status', true);
    }
}

async function changeTier(userId, tier) {
    try {
        await fetchWithAuth(`/api/admin/users/${userId}/tier`, {
            method: 'PUT',
            body: JSON.stringify({ tier })
        });
        showToast(`User upgraded to ${tier}`);
        loadUsers();
    } catch (e) {
        showToast('Failed to change tier', true);
    }
}

async function sendBroadcast() {
    const message = document.getElementById('broadcastMessage').value.trim();
    const targetTier = document.getElementById('broadcastTier').value;

    if (!message) {
        showToast('Please enter a message to broadcast', true);
        return;
    }

    const statusEl = document.getElementById('broadcastStatus');
    statusEl.style.display = 'block';
    statusEl.innerText = '⏳ Dispatching broadcast safely to Telegram users...';

    try {
        const result = await fetchWithAuth('/api/admin/broadcast', {
            method: 'POST',
            body: JSON.stringify({ message, targetTier })
        });
        statusEl.innerText = `✅ Broadcast Completed: ${result.successfullySent} sent, ${result.failed} failed out of ${result.totalTargeted} users.`;
        document.getElementById('broadcastMessage').value = '';
        showToast('Broadcast sent successfully!');
    } catch (e) {
        statusEl.innerText = '❌ Failed to broadcast: ' + e.message;
        showToast('Broadcast failed', true);
    }
}

async function loadCostBreakdown() {
    try {
        const stats = await fetchWithAuth('/api/admin/stats');
        const costs = stats.costBreakdown || [];
        const tbody = document.getElementById('costTableBody');
        if (!costs.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">No AI cost logs recorded yet.</td></tr>';
            return;
        }

        tbody.innerHTML = costs.map(c => `
            <tr>
                <td><strong>${c.model}</strong></td>
                <td>${c.requestCount.toLocaleString()}</td>
                <td>${c.totalTokens.toLocaleString()}</td>
                <td><strong>$${Number(c.estimatedCost).toFixed(6)}</strong></td>
            </tr>
        `).join('');
    } catch (e) {}
}

async function checkBotHealth() {
    try {
        const res = await fetch('/api/telegram/health');
        if (res.ok) {
            const data = await res.json();
            document.getElementById('cfg-mode').innerText = `${data.mode.toUpperCase()} (@${data.botUsername})`;
        }
    } catch (e) {}
}

function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.style.borderLeftColor = isError ? '#ef4444' : '#3b82f6';
    toast.style.display = 'block';
    setTimeout(() => { toast.style.display = 'none'; }, 3500);
}
