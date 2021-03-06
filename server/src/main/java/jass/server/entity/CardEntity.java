/*
 * fhnw-jass is jass game programmed in java for a school project.
 * Copyright (C) 2020 Manuele Vaccari & Victor Hargrave & Thomas Weber & Sasa
 * Trajkova
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jass.server.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jass.lib.database.Entity;
import jass.lib.message.CardData;

/**
 * A model with all known (and cached) Games.
 *
 * @author Victor Hargrave & Manuele Vaccari
 * @version %I%, %G%
 * @since 1.0.0
 */
@DatabaseTable(tableName = "card")
public final class CardEntity extends Entity {
    /**
     * The ID.
     */
    @DatabaseField(id = true)
    private int id;

    /**
     * The rank.
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private RankEntity rank;

    /**
     * The suit.
     */
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private SuitEntity suit;

    /**
     * For ORMLite all persisted classes must define a no-arg constructor with
     * at least package visibility.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public CardEntity() {
    }

    /**
     * @return Returns the ID.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public int getId() {
        return id;
    }

    /**
     * @param id The ID.
     *
     * @return Returns the object for further processing.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public CardEntity setId(final int id) {
        this.id = id;
        return this;
    }

    /**
     * @return Returns the rank.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public RankEntity getRank() {
        return rank;
    }

    /**
     * @param rank The rank.
     *
     * @return Returns the object for further processing.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public CardEntity setRank(final RankEntity rank) {
        this.rank = rank;
        return this;
    }

    /**
     * @return Returns the suit.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public SuitEntity getSuit() {
        return suit;
    }

    /**
     * @param suit The suit.
     *
     * @return Returns the object for further processing.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public CardEntity setSuit(final SuitEntity suit) {
        this.suit = suit;
        return this;
    }

    /**
     * @param cardEntity The card.
     *
     * @return Returns a card DTO.
     *
     * @author Victor Hargrave
     * @since 1.0.0
     */
    public static CardData toCardData(final CardEntity cardEntity) {
        return new CardData(cardEntity.id, cardEntity.suit.getId(), cardEntity.suit.getKey(), cardEntity.rank.getId(), cardEntity.rank.getKey());
    }

    /**
     * @param o The card.
     *
     * @return Returns whether it's the same card or not.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardEntity that = (CardEntity) o;

        return id == that.id;
    }

    /**
     * @return Returns the hashcode.
     *
     * @author Manuele Vaccari
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return id;
    }
}
