// session.js
// Responsible for loading the current session from backend (/api/users/me),
// keeping a small UI cache in localStorage (not source of truth),
// and notifying the app when the session/role changes.
//
// Usage (global):
//   AppSession.init();                    // optional (auto-init on DOMContentLoaded)
//   AppSession.onChange(fn)               // subscribe to changes
//   AppSession.loadSession()              // force reload from backend
//   AppSession.logout()                   // call logout and reset frontend
//   AppSession.isLoggedIn(), .getRole(), .getUser()

(function (window) {
    if (window.AppSession) return; // avoid double include

    const STORAGE_KEYS = {
        ROLE: 'role',
        IS_LOGGED_IN: 'isLoggedIn',
        NAME: 'name',
        ID: 'id'
    };

    const defaultState = {
        isLoggedIn: false,
        role: 'GUEST',
        fullName: '',
        id: null
    };

    let state = { ...defaultState };
    const listeners = new Set();

    function broadcast() {
        // dispatch DOM event so non-js-modules can listen
        const e = new CustomEvent('app:session-changed', { detail: { ...state } });
        window.dispatchEvent(e);

        // call registered listeners
        listeners.forEach(fn => {
            try { fn({ ...state }); } catch (err) { console.error('AppSession listener error', err); }
        });
    }

    function setGuestState() {
        state = { ...defaultState };
        // update UI cache (not authoritative)
        try {
            localStorage.setItem(STORAGE_KEYS.ROLE, state.role);
            localStorage.setItem(STORAGE_KEYS.IS_LOGGED_IN, 'false');
            localStorage.removeItem(STORAGE_KEYS.NAME);
            localStorage.removeItem(STORAGE_KEYS.ID);
        } catch (e) { /* ignore storage errors */ }

        broadcast();
    }

    function setUserState(userObj) {
        // expected userObj: { id, email, fullName, role, isLoggedIn }
        state = {
            isLoggedIn: !!userObj?.isLoggedIn,
            role: userObj?.role || 'GUEST',
            fullName: userObj?.fullName || userObj?.name || '',
            id: userObj?.id || null
        };

        // update UI cache (helpful for SSR or faster load, but backend is always source of truth)
        try {
            localStorage.setItem(STORAGE_KEYS.ROLE, state.role);
            localStorage.setItem(STORAGE_KEYS.IS_LOGGED_IN, state.isLoggedIn ? 'true' : 'false');
            if (state.fullName) localStorage.setItem(STORAGE_KEYS.NAME, state.fullName);
            if (state.id) localStorage.setItem(STORAGE_KEYS.ID, String(state.id));
        } catch (e) { /* ignore storage errors */ }

        broadcast();
    }

    async function loadSession() {
        // Call backend to get authoritative session status
        try {
            const res = await fetch('/api/users/me', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (!res.ok) {
                // backend says no session (401) or other error -> guest
                setGuestState();
                return { ok: false, status: res.status };
            }

            const data = await res.json();
            // expected fields: id, email, fullName, role, isLoggedIn
            setUserState(data);
            return { ok: true, data };
        } catch (err) {
            // network error -> set guest to be safe
            console.error('AppSession.loadSession error', err);
            setGuestState();
            return { ok: false, error: err };
        }
    }

    async function login(email, password) {
        // helper: perform login (but userApi.js will usually call this endpoint instead)
        // returns backend response object or throws
        const body = new URLSearchParams();
        body.append('email', email);
        body.append('password', password);

        const res = await fetch('/api/users/login', {
            method: 'POST',
            credentials: 'include',
            body
        });

        if (!res.ok) {
            const errBody = await safeParseJSON(res);
            throw new Error(errBody?.message || 'Login failed');
        }

        const data = await res.json();
        // backend returns user info; still call loadSession to be authoritative
        await loadSession();
        return data;
    }

    async function logout() {
        try {
            const res = await fetch('/api/users/logout', {
                method: 'POST',
                credentials: 'include'
            });
            // regardless of backend result, clear frontend state
            setGuestState();
            return res.ok;
        } catch (err) {
            console.error('AppSession.logout error', err);
            setGuestState();
            return false;
        }
    }

    // convenience: parse JSON without throwing
    async function safeParseJSON(response) {
        try {
            return await response.json();
        } catch (e) {
            return null;
        }
    }

    // lightweight initialization: restore UI cache immediately, then call loadSession()
    function restoreCacheThenLoad() {
        try {
            const cachedIsLoggedIn = localStorage.getItem(STORAGE_KEYS.IS_LOGGED_IN);
            const cachedRole = localStorage.getItem(STORAGE_KEYS.ROLE);
            const cachedName = localStorage.getItem(STORAGE_KEYS.NAME);
            const cachedId = localStorage.getItem(STORAGE_KEYS.ID);

            if (cachedIsLoggedIn === 'true') {
                state.isLoggedIn = true;
                state.role = cachedRole || 'GUEST';
                state.fullName = cachedName || '';
                state.id = cachedId ? Number(cachedId) : null;
            } else {
                state = { ...defaultState };
            }
        } catch (e) {
            state = { ...defaultState };
        }

        // notify initial state quickly
        broadcast();

        // authoritative load from backend (may switch to Guest)
        loadSession();
    }

    // public API
    const AppSession = {
        init: restoreCacheThenLoad,
        loadSession,
        login,      // optional convenience: use from UI if you want
        logout,
        isLoggedIn: () => !!state.isLoggedIn,
        getRole: () => state.role,
        getUser: () => ({ id: state.id, fullName: state.fullName, role: state.role, isLoggedIn: state.isLoggedIn }),
        onChange: (fn) => {
            if (typeof fn === 'function') {
                listeners.add(fn);
                // return unsubscribe
                return () => listeners.delete(fn);
            }
            return () => {};
        }
    };

    // auto-init on DOMContentLoaded so pages get session quickly
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            AppSession.init();
        });
    } else {
        // already loaded
        AppSession.init();
    }

    // expose globally
    window.AppSession = AppSession;

})(window);
