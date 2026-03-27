import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';

const routes: Routes = [
  { path: 'login', redirectTo: '/tournaments', pathMatch: 'full' },
  { path: 'register', redirectTo: '/tournaments', pathMatch: 'full' },
  { path: '', redirectTo: '/tournaments', pathMatch: 'full' },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AuthRoutingModule {}
