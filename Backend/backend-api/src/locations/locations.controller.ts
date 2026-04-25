import { Controller } from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiResponse,
  ApiParam,
  ApiOkResponse,
} from '@nestjs/swagger';
import { LocationsService } from './locations.service';
import { CacheTTL } from '@nestjs/cache-manager';
import { DAY } from '@/utils/date.utils';
import { TypedParam, TypedRoute } from '@nestia/core';

/**
 * Controller for location data (countries, provinces, cities, currencies)
 * @class LocationsController
 */
@ApiTags('Locations')
@Controller('locations')
@CacheTTL(DAY)
export class LocationsController {
  constructor(private readonly locationsService: LocationsService) {}

  /**
   * Get all countries.
   *
   * Returns a list of all available countries with ISO codes, names, and currency references.
   *
   * @returns {Promise<{ iso_alpha2: string; iso_alpha3: string; iso_numeric: number; name: string; name_local: string; currency_id: number | null }[]>} List of countries
   */
  @TypedRoute.Get('countries')
  @ApiOperation({
    summary: 'Get all countries',
    description:
      'Retrieves a list of all available countries with their details',
  })
  @ApiOkResponse({
    description: 'Successfully retrieved list of countries',
    schema: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          iso_alpha2: {
            type: 'string',
            description: 'ISO 3166-1 alpha-2 country code',
          },
          iso_alpha3: {
            type: 'string',
            description: 'ISO 3166-1 alpha-3 country code',
          },
          iso_numeric: {
            type: 'number',
            description: 'ISO 3166-1 numeric country code',
          },
          name: { type: 'string', description: 'Country name in English' },
          name_local: {
            type: 'string',
            description: 'Country name in local language',
            nullable: true,
          },
          currency_id: {
            type: 'number',
            description: "Reference ID for the country's currency",
          },
        },
        example: {
          iso_alpha2: 'ES',
          iso_alpha3: 'ESP',
          iso_numeric: 724,
          name: 'Spain',
          name_local: 'España',
          currency_id: 978,
        },
      },
    },
  })
  async getCountries(): Promise<
    {
      iso_alpha2: string;
      iso_alpha3: string;
      iso_numeric: number;
      name: string;
      name_local: string;
      currency_id: number | null;
    }[]
  > {
    return this.locationsService.findAllCountries();
  }

  /**
   * Get provinces by country ISO numeric code.
   *
   * @param {number} isoNumeric - ISO 3166-1 numeric country code
   * @returns {Promise<{ id: number; name: string; name_local: string }[]>} List of provinces
   */
  @TypedRoute.Get('countries/:isoNumeric/provinces')
  @ApiOperation({
    summary: 'Get provinces by country',
    description:
      'Retrieves a list of provinces/states for a specific country using its ISO numeric code',
  })
  @ApiParam({
    name: 'isoNumeric',
    description: 'Numeric ISO country code',
    type: Number,
    example: 724,
  })
  @ApiOkResponse({
    description: 'Successfully retrieved list of provinces',
    schema: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'number' },
          name: { type: 'string' },
          name_local: { type: 'string', nullable: true },
        },
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Country not found with the specified ISO numeric code',
  })
  async getProvinces(
    @TypedParam('isoNumeric') isoNumeric: number,
  ): Promise<{ id: number; name: string; name_local: string }[]> {
    return this.locationsService.findProvincesByCountryIsoNumeric(isoNumeric);
  }

  /**
   * Get cities by province ID.
   *
   * @param {number} provinceId - ID of the province
   * @returns {Promise<{ id: number; name: string; name_local: string }[]>} List of cities
   */
  @TypedRoute.Get('provinces/:provinceId/cities')
  @ApiOperation({
    summary: 'Get cities by province',
    description:
      'Retrieves a list of cities for a specific province using its ID',
  })
  @ApiParam({
    name: 'provinceId',
    description: 'ID of the province',
    type: Number,
    example: 1,
  })
  @ApiOkResponse({
    description: 'Successfully retrieved list of cities',
    schema: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'number' },
          name: { type: 'string' },
          name_local: { type: 'string', nullable: true },
        },
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Province not found with the specified ID',
  })
  async getCities(
    @TypedParam('provinceId') provinceId: number,
  ): Promise<{ id: number; name: string; name_local: string }[]> {
    return this.locationsService.findCitiesByProvinceId(provinceId);
  }

  /**
   * Get currency details by ISO numeric code.
   *
   * @param {number} isoNumeric - ISO numeric code for the currency
   * @returns {Promise<{ iso_alpha3: string; symbol: string; decimal_digits: number }[]>} Currency information
   */
  @TypedRoute.Get('currencies/:isoNumeric')
  @ApiOperation({
    summary: 'Get currency details',
    description: 'Retrieves currency information using its ISO numeric code',
  })
  @ApiParam({
    name: 'isoNumeric',
    description: 'Numeric ISO currency code',
    type: Number,
    example: 840,
  })
  @ApiOkResponse({
    description: 'Successfully retrieved currency information',
    schema: {
      type: 'object',
      properties: {
        iso_alpha3: { type: 'string' },
        symbol: { type: 'string' },
        decimal_digits: { type: 'number' },
      },
    },
  })
  @ApiResponse({
    status: 404,
    description: 'Currency not found with the specified ISO numeric code',
  })
  async getCurrency(
    @TypedParam('isoNumeric') isoNumeric: number,
  ): Promise<{ iso_alpha3: string; symbol: string; decimal_digits: number }[]> {
    return this.locationsService.getCurrencyByIsoNumeric(isoNumeric);
  }
}
