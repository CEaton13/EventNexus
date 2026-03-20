import { Injectable, signal, computed } from '@angular/core';
import { OrganizationMembership } from '../../shared/models/organization.model';

const SELECTED_ORG_KEY = 'selectedOrgSlug';

/**
 * TenantService manages the active organization context using Angular Signals.
 *
 * On login, call `setMemberships()` with the list returned by the API.
 * The service restores the previously selected org from localStorage on construction,
 * so the selection survives page reloads.
 *
 * On logout, call `clearTenant()` to reset all state.
 */
@Injectable({
  providedIn: 'root',
})
export class TenantService {
  /** All organizations the authenticated user belongs to. */
  readonly memberships = signal<OrganizationMembership[]>([]);

  /** The currently selected organization membership entry, or null. */
  readonly currentOrg = signal<OrganizationMembership | null>(null);

  /** Slug of the currently selected org, or null. Derived from currentOrg. */
  readonly currentOrgSlug = computed(() => this.currentOrg()?.organizationSlug ?? null);

  /** True when the user has the ORG_ADMIN role in the current org. */
  readonly isOrgAdmin = computed(() => this.currentOrg()?.orgRole === 'ORG_ADMIN');

  constructor() {
    const savedSlug = localStorage.getItem(SELECTED_ORG_KEY);
    if (savedSlug) {
      // Memberships not yet loaded — store slug for matching once setMemberships() is called.
      this._pendingSlug = savedSlug;
    }
  }

  private _pendingSlug: string | null = null;

  /**
   * Populates the memberships signal after login.
   * If a previously selected org slug is saved in localStorage and exists in the
   * provided memberships, it is re-selected automatically. Otherwise the first
   * membership is selected when the list has exactly one entry.
   *
   * @param memberships - org memberships from the login response.
   */
  setMemberships(memberships: OrganizationMembership[]): void {
    this.memberships.set(memberships);

    const slugToRestore = this._pendingSlug;
    this._pendingSlug = null;

    if (slugToRestore) {
      const match = memberships.find(m => m.organizationSlug === slugToRestore);
      if (match) {
        this.currentOrg.set(match);
        return;
      }
    }

    if (memberships.length === 1) {
      this.currentOrg.set(memberships[0]);
      localStorage.setItem(SELECTED_ORG_KEY, memberships[0].organizationSlug);
    }
  }

  /**
   * Sets the active organization by slug and persists the choice to localStorage.
   *
   * @param slug - The org slug to activate.
   */
  selectOrg(slug: string): void {
    const match = this.memberships().find(m => m.organizationSlug === slug);
    if (match) {
      this.currentOrg.set(match);
      localStorage.setItem(SELECTED_ORG_KEY, slug);
    }
  }

  /**
   * Clears all tenant state and removes the persisted slug.
   * Should be called on logout.
   */
  clearTenant(): void {
    this.memberships.set([]);
    this.currentOrg.set(null);
    this._pendingSlug = null;
    localStorage.removeItem(SELECTED_ORG_KEY);
  }
}
