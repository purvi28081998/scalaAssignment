import { Component, OnInit } from "@angular/core";
import { CartService } from "./cart.service";
import {  HttpParams } from '@angular/common/http';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  products: any = [];
  constructor(private cartService: CartService) {}
  ngOnInit(): void {
    this.cartService.getCart().subscribe(
      (data: any) => {
        this.products = data;
      },
      (error) => {
        console.error("There was an error!", error);
      }
    );
  }
  deleteCart(product: any): void {
    const userId = localStorage.getItem('userId');
    if (userId === null) {
        console.error("User ID is null");
        return;
      }
    const cartItem = new HttpParams()
      .set('userID', userId)
      .set('apparelID', product.apparelID.toString())
    

      this.cartService.deleteCart(cartItem).subscribe(
        (response:any) => {
          console.log('Product added to cart:', response);
          this.ngOnInit()
        },
        (error:any) => {
          this.ngOnInit()
          console.error('There was an error adding the product to the cart!', error);
        }
      );
    // Implement your add to cart logic here
  }
}






