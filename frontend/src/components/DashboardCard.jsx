import React from "react";
import PropTypes from "prop-types";
import { Link } from "react-router-dom";


function DashboardCard({ title, subtitle, to = "#", img }) {
  return (
    <Link
      to={to}
      className="bg-white border border-gray-300 rounded-xl p-4 m-3 text-left hover:shadow-md transition flex items-center justify-between h-[10rem]"
      aria-label={title}
    >
      <div className="flex items-center gap-4">
        {img && (
          <img
            src={img}
            alt={`${title} icon`}
            className="w-[7rem] h-[8rem] rounded-md object-fit"
            loading="lazy"
          />
        )}

        <div>
          <h3 className="font-medium">{title}</h3>
          <p className="text-sm text-gray-600">{subtitle}</p>
        </div>
      </div>

      <span className="text-right text-xl" aria-hidden>
        →
      </span>
    </Link>
  );
}

DashboardCard.propTypes = {
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.node]).isRequired,
  subtitle: PropTypes.oneOfType([PropTypes.string, PropTypes.node]),
  to: PropTypes.string,
  img: PropTypes.string,
};

DashboardCard.defaultProps = {
  subtitle: "",
  to: "#",
  img: null,
};

export default React.memo(DashboardCard);