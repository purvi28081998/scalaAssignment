import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
@Injectable({
  providedIn: "root",
})
export class ProductService {
    private apiUrl = "http://35.200.157.92:9007/";
    constructor(private http: HttpClient) {}
  getProducts(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
  addToCart(cartItem: any): Observable<any> {
    return this.http.post<any>("http://35.200.157.92:9003/cart/addCart", cartItem);
  }
  deleteCart(cartItem: any): Observable<any> {
    return this.http.post<any>("http://35.200.157.92:9003/cart/deleteCart", cartItem);
  }
}