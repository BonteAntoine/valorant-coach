// Navigation commune — injectée sur toutes les pages
function renderNav(activePage) {
    const pages = [
        { href: '/',                label: 'Coaching',    key: 'coaching' },
        { href: '/quiz.html',       label: 'Quiz',        key: 'quiz' },
        { href: '/eco.html',        label: 'Éco',         key: 'eco' },
        { href: '/crosshair.html',  label: 'Crosshair',   key: 'crosshair' },
        { href: '/challenges.html', label: 'Challenges',  key: 'challenges' },
        { href: '/lineups.html',    label: 'Lineups',     key: 'lineups' },
    ];

    const nav = document.getElementById('main-nav');
    if (!nav) return;

    nav.innerHTML = `
        <!-- Desktop nav -->
        <div class="hidden md:flex gap-4 text-sm font-bold uppercase tracking-wider">
            ${pages.map(p => `
                <a href="${p.href}" class="${p.key === activePage ? 'text-red-400' : 'text-gray-400 hover:text-white'} transition-colors">
                    ${p.label}
                </a>`).join('')}
        </div>

        <!-- Mobile hamburger -->
        <button id="menu-toggle" class="md:hidden text-gray-400 hover:text-white" onclick="toggleMenu()">
            <svg id="icon-open" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
            </svg>
            <svg id="icon-close" class="w-6 h-6 hidden" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>

        <!-- Mobile dropdown -->
        <div id="mobile-menu" class="hidden absolute top-full left-0 right-0 bg-gray-900 border-b border-gray-800 px-6 py-4 flex flex-col gap-3 z-50 md:hidden">
            ${pages.map(p => `
                <a href="${p.href}" class="${p.key === activePage ? 'text-red-400' : 'text-gray-400'} font-bold uppercase tracking-wider text-sm">
                    ${p.label}
                </a>`).join('')}
        </div>
    `;
}

function toggleMenu() {
    const menu = document.getElementById('mobile-menu');
    const iconOpen = document.getElementById('icon-open');
    const iconClose = document.getElementById('icon-close');
    const isHidden = menu.classList.contains('hidden');
    menu.classList.toggle('hidden', !isHidden);
    iconOpen.classList.toggle('hidden', isHidden);
    iconClose.classList.toggle('hidden', !isHidden);
}
