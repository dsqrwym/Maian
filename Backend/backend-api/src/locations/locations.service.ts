import { Injectable, NotFoundException } from '@nestjs/common';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import {
  cities,
  countries,
  currencies,
  provinces,
} from '#/generated/drizzle/schema.js';
import { eq } from 'drizzle-orm';

@Injectable()
export class LocationsService {
  constructor(private readonly drizzleService: DrizzleService) {}

  async findAllCountries() {
    return this.drizzleService.db.select().from(countries);
  }

  async findProvincesByCountryIsoNumeric(isoNumeric: number) {
    const foundedProvinces = await this.drizzleService.db
      .select({
        id: provinces.id,
        name: provinces.name,
        name_local: provinces.name_local,
      })
      .from(provinces)
      .where(eq(provinces.country_iso, isoNumeric));
    if (foundedProvinces.length === 0) {
      throw new NotFoundException('Provinces not found');
    }
    return foundedProvinces;
  }

  async findCitiesByProvinceId(provinceId: number) {
    const foundedCities = await this.drizzleService.db
      .select({
        id: cities.id,
        name: cities.name,
        name_local: cities.name_local,
      })
      .from(cities)
      .where(eq(cities.province_id, provinceId));
    if (foundedCities.length === 0) {
      throw new NotFoundException('Cities not found');
    }
    return foundedCities;
  }

  async getCurrencyByIsoNumeric(isoNumeric: number) {
    const currency = await this.drizzleService.db
      .select({
        iso_alpha3: currencies.iso_alpha3,
        symbol: currencies.symbol,
        decimal_digits: currencies.decimal_digits,
      })
      .from(currencies)
      .where(eq(currencies.iso_numeric, isoNumeric));

    if (!currency) {
      throw new NotFoundException('Currency not found');
    }
    return currency;
  }
}
