import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { TenantService } from '../services/tenant.service';

/**
 * TenantInterceptor appends the `X-Organization-Slug` header to every
 * outbound HTTP request that targets an org-scoped API path.
 *
 * Auth endpoints (`/api/auth/**`) are excluded — they are unauthenticated
 * and operate before an org context is established.
 */
@Injectable()
export class TenantInterceptor implements HttpInterceptor {
  constructor(private readonly tenantService: TenantService) {}

  /** @inheritdoc */
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const slug = this.tenantService.currentOrgSlug();

    if (!slug || request.url.includes('/api/auth/')) {
      return next.handle(request);
    }

    const tenantRequest = request.clone({
      setHeaders: { 'X-Organization-Slug': slug },
    });

    return next.handle(tenantRequest);
  }
}
