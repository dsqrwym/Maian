import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class LocationsService {
  constructor(private readonly prismaService: PrismaService) {}

  async findAllCountries() {
    const countries = await this.prismaService.countries.findMany();
    if (countries.length === 0) {
      throw new NotFoundException('Countries not found');
    }
    return countries;
  }

  async findProvincesByCountryIsoNumeric(isoNumeric: number) {
    const provinces = await this.prismaService.provinces.findMany({
      where: { country_iso: isoNumeric },
      select: { id: true, name: true, name_local: true },
    });
    if (provinces.length === 0) {
      throw new NotFoundException('Provinces not found');
    }
    return provinces;
  }

  async findCitiesByProvinceId(provinceId: number) {
    const cities = await this.prismaService.cities.findMany({
      where: { province_id: provinceId },
      select: { id: true, name: true, name_local: true },
    });
    if (cities.length === 0) {
      throw new NotFoundException('Cities not found');
    }
    return cities;
  }

  async getCurrencyByIsoNumeric(isoNumeric: number) {
    const currency = await this.prismaService.currencies.findUnique({
      where: { iso_numeric: isoNumeric },
      select: { iso_alpha3: true, symbol: true, decimal_digits: true },
    });
    if (!currency) {
      throw new NotFoundException('Currency not found');
    }
    return currency;
  }
}
