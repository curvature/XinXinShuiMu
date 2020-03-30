package net.newsmth.dirac.data.source;

import net.newsmth.dirac.data.MainPageItem;

import java.util.List;

public interface MainPageDataSource {

    List<MainPageItem> getMainPageItems();

    void saveMainPageItems(List<MainPageItem> mainPageItems);

}
