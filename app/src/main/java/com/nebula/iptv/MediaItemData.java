package com.nebula.iptv;

import java.io.Serializable;

public class MediaItemData implements Serializable {
    public String id, title, subtitle, url, logo, group, kind, subtitleUrl, sourceData;
    public boolean catchup;

    public MediaItemData(String id, String title, String subtitle, String url, String logo, String group, String kind) {
        this.id = id == null ? title : id;
        this.title = title == null ? "Sem título" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.url = url == null ? "" : url;
        this.logo = logo == null ? "" : logo;
        this.group = group == null ? "Outros" : group;
        this.kind = kind == null ? "live" : kind;
        this.subtitleUrl = "";
        this.sourceData = "";
    }
}
