/*
 * Copyright (C) 2013  Christian Autermann, Jan Alexander Wirwahn,
 *                     Arne De Wall, Dustin Demuth, Saqib Rasheed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.car.server.core.filter;

import com.vividsolutions.jts.geom.Geometry;

import io.car.server.core.entities.User;
import io.car.server.core.util.Pagination;

/**
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class TrackFilter {
    private final User user;
    private final Geometry geometry;
    private final Pagination pagination;

    public TrackFilter(User u, Geometry g, Pagination p) {
        this.user = u;
        this.geometry = g;
        this.pagination = p;
    }

    public TrackFilter(Geometry g, Pagination p) {
        this(null, g, p);
    }

    public TrackFilter(User u, Pagination p) {
        this(u, null, p);
    }

    public TrackFilter(Pagination p) {
        this(null, null, p);
    }

    public TrackFilter() {
        this(null, null, null);
    }

    public User getUser() {
        return user;
    }

    public boolean hasUser() {
        return user != null;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public boolean hasGeometry() {
        return geometry != null;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public boolean hasPagination() {
        return pagination != null;
    }
}