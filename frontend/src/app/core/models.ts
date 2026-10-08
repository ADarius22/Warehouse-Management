// TypeScript mirrors of the backend DTOs (com.darius.wms.*)

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** RFC 9457 error body returned by the backend's GlobalExceptionHandler. */
export interface ProblemDetail {
  status: number;
  title: string;
  detail: string;
  errors?: Record<string, string>;
}

export type UnitOfMeasure = 'PIECE' | 'BOX' | 'PALLET' | 'KG' | 'LITER';
export const UNITS: UnitOfMeasure[] = ['PIECE', 'BOX', 'PALLET', 'KG', 'LITER'];

export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string | null;
  unit: UnitOfMeasure;
  weightKg: number | null;
  minStockLevel: number;
  totalQuantity: number;
  lowStock: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  sku: string;
  name: string;
  description: string | null;
  unit: UnitOfMeasure;
  weightKg: number | null;
  minStockLevel: number;
}

export type Zone = 'RECEIVING' | 'STORAGE' | 'PICKING' | 'SHIPPING';
export const ZONES: Zone[] = ['RECEIVING', 'STORAGE', 'PICKING', 'SHIPPING'];

export interface StorageLocation {
  id: number;
  code: string;
  zone: Zone;
  aisle: string;
  rack: number;
  shelf: number;
  capacity: number;
  usedCapacity: number;
  utilizationPercent: number;
  active: boolean;
}

export interface LocationRequest {
  zone: Zone;
  aisle: string;
  rack: number;
  shelf: number;
  capacity: number;
  active: boolean;
}

export interface StockItem {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  locationId: number;
  locationCode: string;
  zone: Zone;
  quantity: number;
  updatedAt: string;
}

export type MovementType = 'INBOUND' | 'TRANSFER' | 'OUTBOUND';

export interface Movement {
  id: number;
  type: MovementType;
  productId: number;
  sku: string;
  productName: string;
  fromLocation: string | null;
  toLocation: string | null;
  quantity: number;
  reference: string | null;
  createdAt: string;
}

export interface ReceiveStockRequest {
  productId: number;
  locationId: number;
  quantity: number;
  reference: string | null;
}

export interface TransferStockRequest {
  productId: number;
  fromLocationId: number;
  toLocationId: number;
  quantity: number;
  reference: string | null;
}

export interface LowStockItem {
  productId: number;
  sku: string;
  name: string;
  minStockLevel: number;
  totalQuantity: number;
}

export type OrderStatus = 'NEW' | 'PICKED' | 'SHIPPED' | 'CANCELLED';
export const ORDER_STATUSES: OrderStatus[] = ['NEW', 'PICKED', 'SHIPPED', 'CANCELLED'];

export interface OrderSummary {
  id: number;
  orderNumber: string;
  customerName: string;
  status: OrderStatus;
  lineCount: number;
  totalUnits: number;
  createdAt: string;
}

export interface OrderLine {
  id: number;
  productId: number;
  sku: string;
  productName: string;
  quantity: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  customerName: string;
  status: OrderStatus;
  lines: OrderLine[];
  createdAt: string;
  updatedAt: string;
  pickedAt: string | null;
  shippedAt: string | null;
}

export interface CreateOrderRequest {
  customerName: string;
  lines: { productId: number; quantity: number }[];
}

export interface ZoneUtilization {
  zone: Zone;
  locationCount: number;
  totalCapacity: number;
  usedCapacity: number;
  utilizationPercent: number;
}

export interface Dashboard {
  productCount: number;
  activeLocationCount: number;
  totalUnits: number;
  openOrders: number;
  lowStock: LowStockItem[];
  zones: ZoneUtilization[];
  recentMovements: Movement[];
}
