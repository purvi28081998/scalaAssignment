import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { RouterModule } from "@angular/router";
import { ReactiveFormsModule } from "@angular/forms";
import { HttpClientModule } from "@angular/common/http";
import { AppComponent } from "./app.component";
import { TopBarComponent } from "./top-bar/top-bar.component";
import { ProductListComponent } from "./product-list/product-list.component";
import { LoginComponent } from './login/login.component';

import { CartComponent } from "./cart/cart.component";
import { ProductService } from "./product-list/product.service";
import { CartService } from "./cart/cart.service";
import { FormsModule } from '@angular/forms';
import { AuthService } from "./login/auth.service";
import { AuthGuard } from './login/auth.guard';

@NgModule({
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    RouterModule.forRoot([
    { path: "apparels", component: ProductListComponent,canActivate: [AuthGuard] },
    { path: 'cart', component: CartComponent,canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
]),
    HttpClientModule,
    FormsModule
  ],
  declarations: [AppComponent, TopBarComponent, ProductListComponent,CartComponent,
    LoginComponent],
  bootstrap: [AppComponent],
  providers: [ProductService,CartService,AuthService],
})
export class AppModule {}