export type Warehouse = {
  id: number;
  name: string;
  location: string;
  capacity: number;
  lat?: number;
  lng?: number;
};

export type CreateWarehousePayload = {
  name: string;
  location: string;
  capacity: number;
  lat?: number;
  lng?: number;
};
