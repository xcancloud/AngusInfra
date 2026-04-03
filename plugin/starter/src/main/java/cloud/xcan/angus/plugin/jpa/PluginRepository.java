package cloud.xcan.angus.plugin.jpa;

import cloud.xcan.angus.plugin.entity.PluginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PluginRepository extends JpaRepository<PluginEntity, String> {

}

