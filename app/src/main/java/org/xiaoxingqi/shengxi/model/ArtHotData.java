package org.xiaoxingqi.shengxi.model;

import java.util.List;

public class ArtHotData extends BaseRepData{
    private List<ArtHotModel> data;

    public List<ArtHotModel> getData() {
        return data;
    }

    public void setData(List<ArtHotModel> data) {
        this.data = data;
    }

    public static class ArtHotModel {
        private PaintData.PaintBean artwork;
        private int artwork_id;
        private int id;
        private int created_at;

        public PaintData.PaintBean getArtwork() {
            return artwork;
        }

        public void setArtwork(PaintData.PaintBean artwork) {
            this.artwork = artwork;
        }

        public int getArtwork_id() {
            return artwork_id;
        }

        public void setArtwork_id(int artwork_id) {
            this.artwork_id = artwork_id;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCreated_at() {
            return created_at;
        }

        public void setCreated_at(int created_at) {
            this.created_at = created_at;
        }
    }

}
