import { Component, OnInit } from "@angular/core";
import { ProductService } from "./product.service";
import {  HttpParams } from '@angular/common/http';

@Component({
  selector: "app-product-list",
  templateUrl: "./product-list.component.html",
  styleUrls: ["./product-list.component.css"],
})
export class ProductListComponent implements OnInit {
  products: any = [];
  constructor(private productService: ProductService) {}
  ngOnInit(): void {
    this.productService.getProducts().subscribe(
      (data: any) => {
        this.products = data;
      },
      (error) => {
        console.error("There was an error!", error);
      }
    );
  }
  addToCart(product: any): void {
    const userId = localStorage.getItem('userId');
    if (userId === null) {
        console.error("User ID is null");
        return;
      }
    const cartItem = new HttpParams()
      .set('userID', userId)
      .set('apparelID', product.id.toString())
      .set('quantity', 1).set('price', product.price);
    

      this.productService.addToCart(cartItem).subscribe(
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






