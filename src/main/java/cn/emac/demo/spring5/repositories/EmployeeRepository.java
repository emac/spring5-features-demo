package cn.emac.demo.spring5.repositories;

import cn.emac.demo.spring5.domain.Employee;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {

}