import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
@Injectable({
  providedIn: "root",
})
export class CartService {
  constructor(private http: HttpClient) {}
  getCart(): Observable<any[]> {
    return this.http.get<any[]>(`http://35.200.157.92:9003/cart?userID=${localStorage.getItem('userId')}`);
  }
  deleteCart(cartItem: any): Observable<any> {
    return this.http.post<any>("http://35.200.157.92:9003/cart/deleteCart", cartItem);
  }
}