export interface User {
  id: number;
  name: string;
  surname: string;
  email: string;
  cellphone?: string;
  nickname?: string;
  password?: string;
  userType?: string;
  imagen?: string;
  enabled?: boolean;
  accountNonExpired?: boolean;
  accountNonLocked?: boolean;
  credentialsNonExpired?: boolean;
  username?: string;
  authorities?: Array<{authority: string}>;
}

export interface Event {
  id: number;
  creator: User;
  title: string;
  description?: string;
  startDate?: number[];
  endDate?: number[];
  category?: {
    id: number;
    name: string;
  };
  statusEvent?: string;
  eventType?: string;
  imagen?: string;
  winnersCount?: number;
  image?: string;
}

export interface Payment {
  id: number;
  paymentId: string;
  externalReference: string;
  user: User;
  event: Event;
  receiver: User;
  amount: number;
  currency: string;
  paymentMethodId: string;
  paymentTypeId: string;
  status: PaymentStatus;
  statusDetail?: string;
  createdAt: number[];
  updatedAt: number[];
  description?: string;
  transactionId?: string;
  gatewayResponse?: string;
}

export enum PaymentStatus {
  PENDING = 'pending',
  APPROVED = 'approved',
  REJECTED = 'rejected',
  CANCELLED = 'cancelled',
  REFUNDED = 'refunded'
}

export interface PaymentResponse {
  status: number;
  message: string;
  data: Payment[];
}

export interface PaymentSingleResponse {
  status: number;
  message: string;
  data: Payment;
}

export enum PaymentSortType {
  DATE = 'date',
  SENDER = 'sender',
  EVENT = 'event'
}

export interface PaymentFilter {
  sortBy: PaymentSortType;
  sortOrder: 'asc' | 'desc';
  status?: PaymentStatus;
  userId?: number;
  eventId?: number;
}